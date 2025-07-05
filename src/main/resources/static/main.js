document.addEventListener('DOMContentLoaded', () => {
    const shortenBtn = document.getElementById('shortenBtn');
    const urlInput = document.getElementById('urlInput');
    const resultContainer = document.getElementById('resultContainer');
    const longUrlElement = document.getElementById('longUrl');
    const shortUrlElement = document.getElementById('shortUrl');
    const customShortUrlElement = document.getElementById('customShortUrl');
    const expirationElement = document.getElementById('expirationPeriod');
    const domainPrefixElement = document.getElementById('domainPrefix');
    const customPathInput = document.getElementById('customPathInput');
    const customPathBtn = document.getElementById('customPathBtn');
    const expirationSelect = document.getElementById('expirationSelect');
    const errorMessageElement = document.getElementById('errorMessage');
    const mainContent = document.querySelector('.main-content');

    let currentUrlId = null;

    shortenBtn.addEventListener('click', async () => {
        const fullUrl = urlInput.value.trim();

        if (!fullUrl) {
            showError('Please, enter a URL');
            return;
        }

        try {
            const response = await fetch('/api/urls', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [document.querySelector('meta[name="_csrf_header"]').content]:
                        document.querySelector('meta[name="_csrf"]').content
                },
                body: JSON.stringify({
                    fullUrl
                })
            });

            if (response.ok) {
                const urlId = await response.text();
                currentUrlId = urlId;
                await displayUrlInfo(urlId);
                domainPrefixElement.textContent = window.location.origin + '/str/';
                urlInput.value = '';
                resultContainer.classList.remove('hidden');
                mainContent.style.marginBottom = '0';
            } else {
                const error = await response.json();
                showError(error.message || 'Error shortening URL');
            }
        } catch (error) {
            console.error('Error:', error);
            showError('An error occurred while shortening the URL');
        }
    });

    customPathBtn.addEventListener('click', async () => {
        const customPath = customPathInput.value.trim();

        if (!customPath) {
            showError('Please, enter a custom path');
            return;
        }

        if (!isValidCustomPath(customPath)) {
            showError('Custom path contains invalid characters. Only letters, numbers, hyphens \'-\' and underscores \'_\' are allowed.');
            return;
        }

        try {
            const response = await fetch(`/api/urls/${currentUrlId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [document.querySelector('meta[name="_csrf_header"]').content]:
                        document.querySelector('meta[name="_csrf"]').content
                },
                body: JSON.stringify({
                    customPath
                })
            });

            if (response.ok) {
                await displayUrlInfo(currentUrlId);
                customPathInput.value = '';
                errorMessageElement.textContent = '';
            } else if (response.status === 409) {
                showError('This custom path is already in use. Please choose another one.');
            } else {
                const error = await response.json();
                showError(error.message || 'Error setting custom path');
            }
        } catch (error) {
            console.error('Error:', error);
            showError('An error occurred while setting custom path');
        }
    });

    async function displayUrlInfo(urlId) {
        try {
            const response = await fetch(`/api/urls/${urlId}`);
            if (response.ok) {
                const urlData = await response.json();
                longUrlElement.textContent = urlData.fullUrl;
                shortUrlElement.textContent = urlData.shortUrl || 'Generating...';
                customShortUrlElement.textContent = urlData.customShortUrl || 'none';

                // Format expiration period display
                if (urlData.expirationPeriod) {
                    const months = Math.floor(urlData.expirationPeriod / 30);
                    expirationElement.textContent = `${urlData.expirationPeriod} days (${months} months)`;
                } else {
                    expirationElement.textContent = 'Not specified';
                }
            } else {
                throw new Error('Failed to fetch URL info');
            }
        } catch (error) {
            console.error('Error fetching URL info:', error);
            showError('Failed to load URL information');
        }
    }

    expirationSelect.addEventListener('change', async () => {
        if (!currentUrlId) return;

        const expirationDays = parseInt(expirationSelect.value) || 90;

        try {
            const response = await fetch(`/api/urls/${currentUrlId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [document.querySelector('meta[name="_csrf_header"]').content]:
                        document.querySelector('meta[name="_csrf"]').content
                },
                body: JSON.stringify({
                    expirationPeriod: expirationDays
                })
            });

            if (response.ok) {
                await displayUrlInfo(currentUrlId); // Refresh the view
            } else {
                const error = await response.json();
                showError(error.message || 'Error updating expiration period');
            }
        } catch (error) {
            console.error('Error:', error);
            showError('An error occurred while updating expiration period');
        }
    });

    function isValidCustomPath(path) {
        // Only allow letters, numbers, hyphens and underscores
        return /^[a-zA-Z0-9-_]+$/.test(path);
    }

    function showError(message) {
        errorMessageElement.textContent = message;
    }
});