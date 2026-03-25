# Project Overview: Market Intelligence Engine (Team Targaryen)
Build a distributed system that tracks competitor changes, extracts structured insights, and generates actionable business strategies using LLMs. 

## Tech Stack
- **Main Application (`/backend`)**: Java (Spring Boot), Spring Web, Spring Data JPA.
- **Scraping Microservice (`/scraper`)**: Python, FastAPI, Playwright.
- **Data Layer**: PostgreSQL.
- **Client Layer (`/frontend`)**: React, Tailwind CSS, Axios.
- **External Services**: Google Gemini API, News API.

---

# PHASE 0: PROJECT SETUP

## Backend (`/backend`)
1. Initialize Spring Boot project with Spring Web, Spring Data JPA, PostgreSQL Driver, Validation, and Scheduling.
2. Setup standard folder structure: controller, service, repository, model, util.

## Scraper (`/scraper`)
1. Initialize FastAPI project.
2. Install dependencies: `fastapi`, `uvicorn`, `playwright`.
3. Run `playwright install`.

## Frontend (`/frontend`)
1. Initialize React app (Vite preferred).
2. Install `axios` and `tailwindcss`.

---

# PHASE 1: DATA LAYER & SCORING SIMPLIFICATION (`/backend`)

## Entities
1. Create `Competitor`: id, name, url.
2. Create `Snapshot`: id, competitorId, contentHash, rawContent, timestamp.
3. Create `ProductOffer`: id, name, currentPrice, previousPrice, competitorPrice.
4. Create `Notification`: id, message, isRead, timestamp, score (Integer), impactLevel (String).
5. Create `Review`: id, competitorId, content, rating.

## Repositories & Seeding
1. Create JPA repositories for all entities.
2. Create `DataSeeder` to insert sample competitors, product offers, and mock reviews.

---

# PHASE 2: SCRAPING MICROSERVICE (`/scraper`)

## Endpoint Implementation
1. Create `POST /scrape` accepting `{"url": "..."}` and returning `{"content": "..."}`.
2. Implement Playwright logic to launch the browser, navigate to the URL, and wait for network idle.
3. Extract ONLY `document.body.innerText` to get clean, readable text, stripping out HTML tags and scripts. Do not use regex for pricing; rely on the LLM for extraction later.

---

# PHASE 3: CHANGE DETECTION & DEMO FALLBACK (`/backend`)

## Core Logic
1. Create `ScrapingClientService` to call the FastAPI endpoint.
2. Implement an MD5 or SHA-256 hashing utility.
3. Create a `MarketSyncService` cron job running every 5 minutes.
4. Workflow: Fetch competitors -> Scrape -> Generate hash -> Compare with last snapshot. If changed, save the new snapshot and trigger the diff extraction.

## The Demo Fallback (Crucial)
1. Add a `mockMode` boolean flag in `application.properties`.
2. Update `ScrapingClientService`: If `mockMode` is true, read raw text from a local JSON file instead of calling the live scraper.

---

# PHASE 4: DIFF EXTRACTION (`/backend`)

## Pre-Processing before LLM
1. Implement basic text comparison logic (e.g., using a Java diff library or basic string chunking) to find changed paragraphs between the old snapshot and the new snapshot.
2. Create a `DiffResult` object containing only the isolated changed text, rather than the entire website copy.

---

# PHASE 5: GEMINI INTELLIGENCE ENGINE (`/backend`)

## API Integration & Strict JSON
1. Create `GeminiService` to call the Google Gemini API.
2. **Crucial:** Configure the API request to use `response_mime_type: application/json`.
3. Create `POST /api/analyze`.
4. Aggregate the latest text diffs, user reviews, and product offers into a single context block.

## Prompt Engineering
1. Construct a system prompt enforcing a strict JSON schema output.
2. Require the LLM to output: `whitespace` (market gaps), `strategy` (positioning advice), `pricing_action` (feasible counter-offers), and `evidence` (the exact source text triggering the insight).

---

# PHASE 6: ACTION & SCORING ENGINE (`/backend`)

## Insight Processing
1. Parse the JSON response from Gemini.
2. If `pricing_action` exists, generate a new `Notification`.
3. Calculate a simple `score` (1-10) based on whether it is a pricing change (high impact) or a messaging change (lower impact), and save it to the `Notification` entity.

---

# PHASE 7: FRONTEND DASHBOARD (`/frontend`)

## Layout & Views
1. Build a Sidebar and a Top Header with a Notification Bell.
2. **Dashboard View:** Display insights, strategy, and high-score pricing recommendations fetched from `/api/analyze`.
3. **Competitor Tracking View:** Show a visual timeline of detected changes.
4. **News Integration:** Fetch sector news from NewsAPI and display it in a side widget.

---

# PHASE 8: TRACEABILITY & POLISH (`/frontend`)

## UI Finalization
1. Ensure every insight displayed explicitly shows its source (e.g., "Source: Competitor Pricing Page, 2 hours ago").
2. Verify the end-to-end flow works seamlessly with the `mockMode` toggle active for the presentation.