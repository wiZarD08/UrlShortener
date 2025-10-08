let utmSupport = true;
const path = window.location.pathname;
const urlId = path.split('/').pop();
const userTimezoneOffset = -new Date().getTimezoneOffset() / 60;

document.addEventListener('DOMContentLoaded', function () {

    if (!urlId || isNaN(urlId)) {
        document.getElementById('stats-content').innerHTML =
            '<div class="error-message">Error: No URL ID provided. Please return to your profile and try again.</div>';
    } else {
        loadUrlDetails(urlId);
        loadVisitorStatistics(urlId);
        loadDateStatistics(urlId, userTimezoneOffset);
        loadTimeStatistics(urlId, userTimezoneOffset);
        loadUtmStatistics(urlId);
    }
});

function loadUrlDetails(urlId) {
    fetch(`/api/urls/${urlId}`)
        .then(response => {
            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error('URL not found');
                }
                throw new Error('Failed to load URL details');
            }
            return response.json();
        })
        .then(urlDto => {
            displayUrlDetails(urlDto);
        })
        .catch(error => {
            console.error('Error loading URL details:', error);
            document.getElementById('url-info-container').innerHTML =
                `<div class="error-message">Error loading URL details: ${error.message}</div>`;
        });
}

function displayUrlDetails(urlDto) {
    document.getElementById('full-url-display').textContent = urlDto.fullUrl || 'N/A';
    document.getElementById('short-url-display').textContent = urlDto.shortUrl || 'N/A';

    if (urlDto.customShortUrl) {
        document.getElementById('custom-url-display').textContent = urlDto.customShortUrl;
        document.getElementById('custom-url-container').style.display = 'flex';
    }

    if (urlDto.expirationPeriod > 0) {
        document.getElementById('days-left-display').textContent = `${urlDto.expirationPeriod} days`;
        document.getElementById('days-left-display').style.color = urlDto.expirationPeriod < 30 ? '#e74c3c' : '#27ae60';
    } else {
        document.getElementById('days-left-display').textContent = 'Expired';
    }

    document.getElementById('total-visitors').textContent = urlDto.clicks;
    document.getElementById('unique-visitors').textContent = urlDto.uniqueClicks;

    if (urlDto.utmSupport === false) utmSupport = false;
}

function loadVisitorStatistics(urlId) {
    fetch(`/api/stats/${urlId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load visitor statistics');
            }
            return response.json();
        })
        .then(statisticsData => {
            displayVisitorStatistics(statisticsData);
        })
        .catch(error => {
            alert("caught error ", error, error.message);
            console.error('Error loading visitor statistics:', error);
            document.getElementById('stats-overview').innerHTML =
                '<div class="error-message">Error loading visitor statistics. Please try again.</div>';
        });
}

function displayVisitorStatistics(statisticsData) {
    console.log('Displaying statistics data:', statisticsData);

    if (!statisticsData || statisticsData.length === 0) {
        console.log('No statistics data available');
        document.getElementById('stats-overview').innerHTML =
            '<div class="no-data">No visitor statistics available for this URL</div>';
        return;
    }

    try {
        const countries = [...new Set(statisticsData.map(stat => stat.country))];
        const uniqueCountries = countries.length;
        console.log('Unique countries:', uniqueCountries, countries);

        // Device statistics
        const deviceCounts = statisticsData.reduce((acc, stat) => {
            const device = stat.device || 'Unknown';
            acc[device] = (acc[device] || 0) + 1;
            return acc;
        }, {});

        // Browser statistics
        const browserCounts = statisticsData.reduce((acc, stat) => {
            const agent = stat.agent || 'Unknown';
            acc[agent] = (acc[agent] || 0) + 1;
            return acc;
        }, {});

        // OS statistics
        const osCounts = statisticsData.reduce((acc, stat) => {
            const os = stat.os || 'Unknown';
            acc[os] = (acc[os] || 0) + 1;
            return acc;
        }, {});

        // Country and city statistics
        const countryCityCounts = statisticsData.reduce((acc, stat) => {
            const country = stat.country || 'Unknown';
            const city = stat.city || 'Unknown';

            if (!acc[country]) {
                acc[country] = { total: 0, cities: {} };
            }

            acc[country].total += 1;
            acc[country].cities[city] = (acc[country].cities[city] || 0) + 1;
            return acc;
        }, {});

        const topCountry = Object.entries(countryCityCounts)
            .sort(([, a], [, b]) => b.total - a.total)[0] || ['-', { total: 0 }];


        document.getElementById('unique-countries').textContent = uniqueCountries;
        document.getElementById('top-country').textContent = topCountry[0];

        console.log('Creating charts...');

        createDeviceChart(deviceCounts);
        createBrowserChart(browserCounts);
        createOSChart(osCounts);
        createCountryCityList(countryCityCounts);

        console.log('Charts created successfully');

    } catch (error) {
        console.error('Error in displayVisitorStatistics:', error);
        document.getElementById('stats-overview').innerHTML =
            `<div class="error-message">Error displaying statistics: ${error.message}</div>`;
    }
}

function createDeviceChart(deviceCounts) {
    try {
        const canvas = document.getElementById('deviceChart');
        if (!canvas) {
            console.error('Device chart canvas not found');
            return;
        }

        const processedData = processChartData(deviceCounts);
        console.log('Creating device chart with data:', processedData);

        const ctx = canvas.getContext('2d');
        const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#FFA07A', '#20B2AA'];

        new Chart(ctx, {
            type: 'pie',
            data: {
                labels: processedData.labels,
                datasets: [{
                    data: processedData.data,
                    backgroundColor: colors,
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
        console.log('Device chart created successfully');
    } catch (error) {
        console.error('Error creating device chart:', error);
    }
}

function createBrowserChart(browserCounts) {
    try {
        const canvas = document.getElementById('browserChart');
        if (!canvas) {
            console.error('Browser chart canvas not found');
            return;
        }

        const processedData = processChartData(browserCounts);
        console.log('Creating browser chart with data:', processedData);

        const ctx = canvas.getContext('2d');
        const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#FFA07A', '#20B2AA'];

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: processedData.labels,
                datasets: [{
                    data: processedData.data,
                    backgroundColor: colors,
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
        console.log('Browser chart created successfully');
    } catch (error) {
        console.error('Error creating browser chart:', error);
    }
}

function createOSChart(osCounts) {
    try {
        const canvas = document.getElementById('osChart');
        if (!canvas) {
            console.error('OS chart canvas not found');
            return;
        }

        const processedData = processChartData(osCounts);
        console.log('Creating OS chart with data:', processedData);

        const ctx = canvas.getContext('2d');
        const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD', '#FFA07A', '#20B2AA'];

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: processedData.labels,
                datasets: [{
                    data: processedData.data,
                    backgroundColor: colors,
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
        console.log('OS chart created successfully');
    } catch (error) {
        console.error('Error creating OS chart:', error);
    }
}

// Helper function to process chart data - get top 7 and group the rest as "Other"
function processChartData(counts) {
    const sorted = Object.entries(counts)
        .sort(([, a], [, b]) => b - a);

    let labels = [];
    let data = [];

    if (sorted.length <= 7) {
        labels = sorted.map(([label]) => label);
        data = sorted.map(([, count]) => count);
    } else {
        const top7 = sorted.slice(0, 7);
        const others = sorted.slice(7);

        const othersSum = others.reduce((sum, [, count]) => sum + count, 0);

        labels = [...top7.map(([label]) => label), 'Other'];
        data = [...top7.map(([, count]) => count), othersSum];
    }

    return { labels, data };
}

function createCountryCityList(countryCityCounts) {
    try {
        const countryList = document.getElementById('countryList');

        const sortedCountries = Object.entries(countryCityCounts)
            .sort(([, a], [, b]) => b.total - a.total);

        countryList.innerHTML = sortedCountries.map(([country, data]) => {
            const sortedCities = Object.entries(data.cities)
                .sort(([, a], [, b]) => b - a);

            const citiesHtml = sortedCities.map(([city, count]) => `
                <div class="city-row" style="display: flex; justify-content: space-between; padding: 2px 0; font-size: 12px; color: #666;">
                    <span class="city-name" style="flex: 1;">${city}</span>
                    <span class="city-count" style="font-weight: bold; color: #2c5aa0;">${count}</span>
                </div>
            `).join('');

            return `
                <div class="country-section" style="margin-bottom: 15px; border-bottom: 1px solid #eee; padding-bottom: 10px;">
                    <div class="country-row" style="display: flex; justify-content: space-between; font-weight: bold; margin-bottom: 5px;">
                        <span class="country-name">${country}</span>
                        <span class="country-count" style="color: #2c5aa0;">${data.total}</span>
                    </div>
                    <div class="cities-container">
                        ${citiesHtml}
                    </div>
                </div>
            `;
        }).join('');

        console.log('Country city list created successfully');
    } catch (error) {
        console.error('Error creating country city list:', error);
    }
}

function loadDateStatistics(urlId, timeZone) {
    fetch(`/api/stats/date/${urlId}?timeZone=${timeZone}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load date statistics');
            }
            return response.json();
        })
        .then(dateData => {
            createDailyClicksChart(dateData);
        })
        .catch(error => {
            console.error('Error loading date statistics:', error);
        });
}

function loadTimeStatistics(urlId, timeZone) {
    fetch(`/api/stats/time/${urlId}?timeZone=${timeZone}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load time statistics');
            }
            return response.json();
        })
        .then(timeData => {
            createHourlyActivityChart(timeData);
        })
        .catch(error => {
            console.error('Error loading time statistics:', error);
        });
}

function createDailyClicksChart(dateData) {
    try {
        const canvas = document.getElementById('dailyClicksChart');
        if (!canvas) {
            console.error('Daily clicks chart canvas not found');
            return;
        }

        const sortedData = dateData.sort((a, b) => a.date.localeCompare(b.date));

        const labels = sortedData.map(item => {
            const date = new Date(item.date);
            return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
        });

        const clicks = sortedData.map(item => item.clicks);

        const ctx = canvas.getContext('2d');

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Clicks',
                    data: clicks,
                    backgroundColor: 'rgba(160, 216, 179, 0.2)',
                    borderColor: '#a0d8b3',
                    borderWidth: 2,
                    tension: 0.3,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });

        console.log('Daily clicks chart created successfully');
    } catch (error) {
        console.error('Error creating daily clicks chart:', error);
    }
}

function createHourlyActivityChart(timeData) {
    try {
        const canvas = document.getElementById('hourlyActivityChart');
        if (!canvas) {
            console.error('Hourly activity chart canvas not found');
            return;
        }

        // Create labels for 24 hours
        const labels = Array.from({ length: 24 }, (_, i) => {
            return `${i.toString().padStart(2, '0')}:00`;
        });

        const ctx = canvas.getContext('2d');

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Clicks',
                    data: timeData,
                    backgroundColor: 'rgba(74, 107, 255, 0.6)',
                    borderColor: '#4a6bff',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });

        console.log('Hourly activity chart created successfully');
    } catch (error) {
        console.error('Error creating hourly activity chart:', error);
    }
}

function loadUtmStatistics(urlId) {
    fetch(`/api/stats/utm/${urlId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load UTM statistics');
            }
            return response.json();
        })
        .then(utmData => {
            displayUtmStatistics(utmData);
        })
        .catch(error => {
            console.error('Error loading UTM statistics:', error);
            document.getElementById('stats-content').innerHTML =
                '<div class="error-message">Error loading UTM statistics. Please try again.</div>';
        });
}

function displayUtmStatistics(utmData) {
    const container = document.getElementById('stats-content');
    const utmGenerator = document.getElementById('utm-generator-container');

    if (!utmSupport) {
        container.innerHTML = '<div class="no-data">No UTM statistics available for this URL</div>';
        utmGenerator.style.display = 'none';
        return;
    }

    const totalClicks = utmData.reduce((sum, utm) => sum + (utm.clicks || 0), 0);

    let tableHTML = `
                <div class="summary-info">
                    <p class="summary-text">Total UTM variations: ${utmData.length}  |  Total UTM clicks: ${totalClicks}</p>
                </div>
                <table class="stats-table">
                    <thead>
                        <tr>
                            <th>Source</th>
                            <th>Medium</th>
                            <th>Campaign</th>
                            <th>Content</th>
                            <th>Clicks</th>
                            <th>Percentage</th>
                        </tr>
                    </thead>
                    <tbody>
            `;

    utmData.forEach(utm => {
        const percentage = totalClicks > 0 ? ((utm.clicks || 0) / totalClicks * 100).toFixed(1) : 0;
        tableHTML += `
                    <tr>
                        <td>${escapeHtml(utm.source || '-')}</td>
                        <td>${escapeHtml(utm.medium || '-')}</td>
                        <td>${escapeHtml(utm.campaign || '-')}</td>
                        <td>${escapeHtml(utm.content || '-')}</td>
                        <td class="clicks-cell">${utm.clicks || 0}</td>
                        <td class="percentage-cell">${percentage}%</td>
                    </tr>
                `;
    });

    tableHTML += `
                    </tbody>
                </table>
            `;

    container.innerHTML = tableHTML;
    utmGenerator.style.display = 'block';
}

function copyToClipboard(type) {
    let textToCopy = '';
    if (type === 'short') {
        textToCopy = document.getElementById('short-url-display').textContent;
    } else if (type === 'custom') {
        textToCopy = document.getElementById('custom-url-display').textContent;
    }

    navigator.clipboard.writeText(textToCopy).then(() => {
        const button = event.target;
        const originalText = button.textContent;
        button.textContent = '✓';
        button.style.color = '#27ae60';
        setTimeout(() => {
            button.textContent = originalText;
            button.style.color = '';
        }, 2000);
    });
}

function escapeHtml(unsafe) {
    if (unsafe === null || unsafe === undefined) return '-';
    return unsafe.toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function goBack() {
    window.location.href = '/profile';
}

document.getElementById('utmForm').addEventListener('submit', function (e) {
    e.preventDefault();
    generateUtmUrls();
});

function generateUtmUrls() {
    const source = document.getElementById('source').value.trim();
    const medium = document.getElementById('medium').value.trim();
    const campaign = document.getElementById('campaign').value.trim();
    const content = document.getElementById('content').value.trim();

    clearErrors();

    const utmTagDto = {
        source: source,
        medium: medium,
        campaign: campaign,
        content: content || null
    };

    const generateBtn = document.getElementById('generateBtn');
    generateBtn.textContent = 'Generating...';
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    fetch(`/utm/generate/${urlId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify(utmTagDto)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(errorData => {
                    throw errorData;
                });
            }
            return response.json();
        })
        .then(data => {
            displayResults(data);
            showSuccess('URLs generated successfully!');
        })
        .catch(errorData => {
            console.error('Validation error:', errorData);
            displayValidationErrors(errorData);
        })
        .finally(() => {
            generateBtn.textContent = 'Generate UTM URLs';
        });
}

function displayValidationErrors(errorData) {
    clearErrors();

    console.log('Error data:', errorData);

    if (errorData.errors && Array.isArray(errorData.errors)) {
        errorData.errors.forEach(error => {
            const field = error.field; // "medium", "source", etc.
            const message = error.defaultMessage;

            const errorElementId = field + 'Error';
            showError(errorElementId, message);
        });
    } else if (errorData.message) {
        showError('generalError', errorData.message);
    } else {
        showError('generalError', 'Validation failed. Please check your inputs.');
    }
}

function displayResults(data) {
    const resultsContainer = document.getElementById('resultsContainer');
    const shortUrlResult = document.getElementById('shortUrlResult');
    const customUrlResult = document.getElementById('customUrlResult');
    const customUrlResultItem = document.getElementById('customUrlResultItem');

    shortUrlResult.textContent = data.shortUrl || 'N/A';

    if (data.customShortUrl) {
        customUrlResult.textContent = data.customShortUrl;
        customUrlResultItem.style.display = 'block';
    } else {
        customUrlResultItem.style.display = 'none';
    }

    resultsContainer.style.display = 'block';
}

function copyResult(type) {
    let textToCopy = '';
    if (type === 'short') {
        textToCopy = document.getElementById('shortUrlResult').textContent;
    } else if (type === 'custom') {
        textToCopy = document.getElementById('customUrlResult').textContent;
    }

    navigator.clipboard.writeText(textToCopy).then(() => {
        const button = event.target;
        const originalText = button.textContent;
        button.textContent = '✓ Copied!';
        button.style.background = '#27ae60';
        button.style.borderColor = '#27ae60';
        button.style.color = 'white';

        setTimeout(() => {
            button.textContent = originalText;
            button.style.background = '';
            button.style.borderColor = '#a0d8b3';
            button.style.color = '#a0d8b3';
        }, 2000);
    });
}

function clearErrors() {
    const errorElements = document.querySelectorAll('.error-message');
    errorElements.forEach(el => {
        el.style.display = 'none';
        el.textContent = '';
    });
}

function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    errorElement.textContent = message;
    errorElement.style.display = 'block';
}

function showSuccess(message) {
    const successElement = document.getElementById('successMessage');
    successElement.textContent = message;
    successElement.style.display = 'block';
    setTimeout(() => {
        successElement.style.display = 'none';
    }, 3000);
}

function openExtendModal() {
    document.getElementById('extendModal').style.display = 'flex';
    document.getElementById('daysInput').value = '';
    document.getElementById('modalError').style.display = 'none';
    document.getElementById('daysInput').focus();
}

function closeExtendModal() {
    document.getElementById('extendModal').style.display = 'none';
}

function extendUrl() {
    const daysInput = document.getElementById('daysInput');
    const days = parseInt(daysInput.value);
    const errorElement = document.getElementById('modalError');

    errorElement.style.display = 'none';

    if (!days || days < 1) {
        errorElement.textContent = 'Please enter a number more than 0';
        errorElement.style.display = 'block';
        return;
    }

    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    const extendBtn = document.querySelector('.modal-btn.primary');
    const originalText = extendBtn.textContent;
    extendBtn.textContent = 'Extending...';
    extendBtn.disabled = true;

    fetch(`/api/urls/${urlId}/add_days`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        credentials: 'include',
        body: JSON.stringify(days)
    })
        .then(response => {
            if (response.redirected) {
                throw new Error('Authentication required. Please log in again.');
            }
            if (!response.ok) {
                return response.json().then(errorData => {
                    throw new Error(errorData.message || `HTTP ${response.status}`);
                });
            }
            return response.json();
        })
        .then(updatedUrlDto => {
            if (updatedUrlDto.expirationPeriod > 0) {
                document.getElementById('days-left-display').textContent = `${updatedUrlDto.expirationPeriod} days`;
                document.getElementById('days-left-display').style.color = updatedUrlDto.expirationPeriod < 30 ? '#e74c3c' : '#27ae60';
            }

            closeExtendModal();
            showSuccess('URL extended successfully!');
        })
        .catch(error => {
            console.error('Error extending URL:', error);
            errorElement.textContent = `${error.message}`;
            errorElement.style.display = 'block';
        })
        .finally(() => {
            extendBtn.textContent = originalText;
            extendBtn.disabled = false;
        });
}

document.getElementById('extendModal').addEventListener('click', function (e) {
    if (e.target === this) {
        closeExtendModal();
    }
});

document.getElementById('daysInput').addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        extendUrl();
    }
});