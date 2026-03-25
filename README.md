# Market Intelligence Engine - Architecture & Execution Guide

## 1. High-Level Architecture
The system is fundamentally a distributed microservice architecture split into 3 nodes:
- **Frontend (`/frontend`)**: A React + Vite dashboard styled with Tailwind CSS. It visualizes AI insights and historical data in a modern UI.
- **Scraping Engine (`/scraper`)**: A Python FastAPI microservice utilizing headless browser automation (Playwright/Chromium) to execute complex, Javascript-heavy scraping.
- **Core Engine (`/backend`)**: A Java Spring Boot application taking care of scheduling (Cron Jobs), change detection (Hashing & Diffing), Data persistence (PostgreSQL), and AI integration (Google Gemini).

---

## 2. Directory & File Breakdown

### A. The Core Engine (`/backend`)
- **`MarketintelApplication.java`**: The Spring Boot entry point. Enables `@EnableScheduling` for our timing mechanisms.
- **`model/`** (JPA Entities):
  - `Competitor.java`: Stores targets we track (e.g., URL and Name).
  - `Snapshot.java`: Records historical scraped data and its SHA-256 hash.
  - `ProductOffer.java` & `Review.java`: Internal pricing and sentiment metrics for context.
  - `Notification.java`: The high-priority AI-generated alerts.
  - `DiffResult.java`: DTO holding isolated code changes between Snapshots.
- **`repository/`**: Spring Data JPA interfaces that automatically translate Java objects to PostgreSQL rows without writing literal SQL. 
- **`service/`**:
  - `MarketSyncService.java`: The beating heart of the chronological engine. Runs every 60 seconds. Commands the crawler, tests the returned hash against the Database for changes, triggers diffs, and conditionally saves.
  - `ScrapingClientService.java`: Reaches out over HTTP to the Python microservice to get raw website text. Contains the critical `mockMode` fallback for safe presentations.
  - `DiffService.java`: Evaluates previous text against new text using Set-intersection logic to isolate exactly what sentences/paragraphs changed.
  - `GeminiService.java`: Connects securely to the Google API (`generativelanguage`) with a JSON strictly-enforced prompt schema.
- **`controller/`**:
  - `AnalyzeController.java`: Exposes `POST /api/analyze`. Gathers the `DiffResult`, `ProductOffer`, and `Review` blocks, feeds them to `GeminiService`, extracts the `pricing_action` and `strategy` JSON from the LLM, scores it for priority (9 vs 5), and persists a `Notification`.
  - `DataController.java`: A simple set of `GET` mappings serving historical `Notifications` and `Snapshots` purely to populate the React interface.
- **`util/HashUtil.java`**: Calculates SHA-256 deterministic fingerprints.
- **`util/DataSeeder.java`**: Automatically triggers exactly once on boot if the Database is empty to populate the system with dummy mock metrics.

### B. The Scrape Microservice (`/scraper`)
- **`main.py`**: A fast, asynchronous FastAPI application. Receives the destination `url` from Spring Boot and spawns `scrape_task.py` as an isolated subprocess.
- **`scrape_task.py`**: Physically boots a Chromium headless browser via Playwright. Navigates to the assigned URL, strictly waits for `networkidle`, and rips out the `document.body.innerText`. Designed to isolate the execution away from standard Uvicorn/Windows async bugs.

### C. The Visual Dashboard (`/frontend`)
- **`src/App.tsx`**: A massive, single-page React component painted dynamically via Tailwind classes. 
  - Triggers the Core Engine analysis endpoint on button click.
  - Renders the LLM Strategy inside a glassmorphism card.
  - Visually aligns EXACTLY what raw text sourced the decision (The Traceability Engine).
  - Iterates real-time histories of "Scraper History" and "System Alerts".
- **`tailwind.config.js` & `index.css`**: Design configurations holding scrolling behaviors and UI theme limits.

---

## 3. How the End-to-End System Runs (The Execution Pipeline)
1. **The Pulse**: A cron job inside Spring Boot (`MarketSyncService`) wakes up every 60 seconds.
2. **The Recon**: Spring Boot loops through its DB targets, sending HTTP calls to Python (`/scraper`).
3. **The Scrape**: Python spawns an invisible Chrome tab, grabs the actual body text, cleanly waits for Javascript to render dynamic prices, and returns pristine text back to Spring Boot.
4. **The Isolation**: Java hashes the new text. If it drifted from the last DB entry, it triggers the `DiffService` to chunk out only the new active sentences, discarding irrelevant boilerplate text, and logs a new `Snapshot`.
5. *(Triggered via UI)* **The Brain**: You click "Refresh Intelligence" in React. Spring Boot's `AnalyzeController` bundles the localized diffs, internal costs, and reviews, injecting them into the Gemini 2.5 Flash model API.
6. **The Intelligence**: Gemini evaluates the prompt and responds strictly in structured JSON format. 
7. **The Scorer**: The backend controller parses out the `pricing_action`. A hardcoded scoring algorithm evaluates the text for urgency (e.g. keywords like "$", "reduce") and sinks it directly into PostgreSQL as a high-priority `Notification` log.
8. **The Render**: React fetches these updated metrics and beautifully populates the timeline matrices, displaying the Strategy right next to the Source Evidence that justified it.
