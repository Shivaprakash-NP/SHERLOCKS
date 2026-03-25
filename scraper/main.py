from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from playwright.async_api import async_playwright

app = FastAPI(title="Market Intelligence Scraper")

class ScrapeRequest(BaseModel):
    url: str

class ScrapeResponse(BaseModel):
    content: str

@app.post("/scrape", response_model=ScrapeResponse)
async def scrape_endpoint(request: ScrapeRequest):
    try:
        async with async_playwright() as p:
            browser = await p.chromium.launch(headless=True)
            context = await browser.new_context()
            page = await context.new_page()
            
            # Navigate to URL, waiting until network connections are idle
            await page.goto(request.url, wait_until="networkidle")
            
            # Extract clean innerText from the body
            content = await page.evaluate("() => document.body.innerText")
            
            await browser.close()
            return ScrapeResponse(content=content)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
