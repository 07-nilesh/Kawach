// scripts/content.js

// Smart Scraper for Member 1
function getCleanLegalText() {
    let textArray = [];
    // Only grab actual text-heavy elements, ignore scripts and styles
    document.querySelectorAll('p, li, h1, h2, h3, h4').forEach(el => {
        if (el.innerText.trim().length > 20) { // Ignore tiny UI buttons
            textArray.push(el.innerText.trim());
        }
    });
    // Join and strictly limit to 2000 characters to prevent AI timeouts
    return textArray.join('\n').substring(0, 2000);
}

// Function to hunt for emails using Regular Expressions (Regex)
function scrapeEmailsAndURL() {
    const url = window.location.href;
    const pageHTML = document.body.innerHTML;

    // Standard Regex pattern to find email addresses
    const emailRegex = /([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\.[a-zA-Z0-9_-]+)/gi;
    const foundEmails = pageHTML.match(emailRegex) || [];

    // Remove duplicates by converting the array to a Set and back
    const uniqueEmails = [...new Set(foundEmails)];

    return {
        url: url,
        emails: uniqueEmails
    };
}

// Listen for instructions from the popup
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === "SCRAPE_TOS") {
        const text = getCleanLegalText();
        sendResponse({ success: true, data: text });
    }
    else if (request.action === "SCRAPE_FRAUD") {
        const fraudData = scrapeEmailsAndURL();
        sendResponse({ success: true, data: fraudData });
    }
    return true; // Keep the message channel open for asynchronous response
});