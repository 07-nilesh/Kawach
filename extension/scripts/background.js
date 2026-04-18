// Kawach Background Service Worker
// Manages extension lifecycle and badge updates

chrome.runtime.onInstalled.addListener(() => {
    console.log("[Kawach] Extension installed. Shield active.");
});

// Listen for messages from popup to relay backend status
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === "healthCheck") {
        fetch("http://localhost:8080/api/v1/health")
            .then(res => res.json())
            .then(data => {
                sendResponse({ online: true, data: data });
            })
            .catch(err => {
                sendResponse({ online: false, error: err.message });
            });
        return true; // async response
    }
});
