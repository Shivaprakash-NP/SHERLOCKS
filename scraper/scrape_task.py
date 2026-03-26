import sys
import json
import asyncio
from playwright.sync_api import sync_playwright
from markdownify import markdownify as md

if sys.platform == "win32":
    asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())

def main():
    if len(sys.argv) < 2:
        print(json.dumps({"success": False, "error": "No URL provided"}))
        sys.exit(1)

    url = sys.argv[1]

    try:
        with sync_playwright() as p:
            browser = p.chromium.launch(headless=True)
            context = browser.new_context()
            page = context.new_page()

            page.goto(url, wait_until="networkidle")

            # Get the raw HTML of the body instead of innerText
            html_content = page.inner_html("body")

            # Convert it to clean Markdown, stripping out links and images to save tokens
            clean_markdown = md(html_content, strip=['a', 'img', 'script', 'style'])

            browser.close()

            print(json.dumps({"success": True, "content": clean_markdown}))

    except Exception as e:
        print(json.dumps({"success": False, "error": str(e)}))

if __name__ == "__main__":
    main()