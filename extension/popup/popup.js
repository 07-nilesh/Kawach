document.addEventListener('DOMContentLoaded', () => {
    const auditBtn = document.getElementById('audit-btn');
    const scanBtn = document.getElementById('scan-btn');
    const resultsContainer = document.getElementById('results-container');

    auditBtn.addEventListener('click', () => {
        console.log("Audit Legal Text button clicked!");
        resultsContainer.innerHTML = "<p class='text-blue-400'>Initializing Legal Audit...</p>";
    });

    scanBtn.addEventListener('click', () => {
        console.log("Scan for Fraud button clicked!");
        resultsContainer.innerHTML = "<p class='text-purple-400'>Initializing Fraud Scan...</p>";
    });
});