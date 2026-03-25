import json
import subprocess
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import sys

app = FastAPI(title="Market Intelligence Scraper")

class ScrapeRequest(BaseModel):
    url: str

class ScrapeResponse(BaseModel):
    content: str

@app.post("/scrape", response_model=ScrapeResponse)
def scrape_endpoint(request: ScrapeRequest):
    try:
        result = subprocess.run(
            [sys.executable, "scrape_task.py", request.url],
            capture_output=True,
            text=True,
            check=True
        )
        
        try:
            data = json.loads(result.stdout.strip())
        except json.JSONDecodeError:
            raise Exception(f"Failed to parse scrape script output: {result.stdout} | stderr: {result.stderr}")
            
        if not data.get("success"):
            raise Exception(data.get("error", "Unknown scraping error"))
            
        return ScrapeResponse(content=data.get("content", ""))
        
    except subprocess.CalledProcessError as e:
        raise HTTPException(status_code=500, detail=f"Subprocess failed. stderr: {e.stderr} | stdout: {e.stdout}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error: {str(e)}")
