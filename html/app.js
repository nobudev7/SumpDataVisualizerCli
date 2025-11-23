document.addEventListener('DOMContentLoaded', () => {
    const navigation = document.getElementById('navigation');
    const charts = document.getElementById('charts');
    let fileTree = {};

    fetch('file-list.json')
        .then(response => response.json())
        .then(data => {
            fileTree = data;
            buildNavigation();
            loadLatest();
        });

    function buildNavigation() {
        const years = Object.keys(fileTree).sort().reverse();
        const navList = document.createElement('ul');

        years.forEach(year => {
            const yearItem = document.createElement('li');
            yearItem.textContent = year;
            yearItem.classList.add('year-heading'); // Add class for styling
            const monthList = document.createElement('ul');
            const months = Object.keys(fileTree[year]).sort().reverse();

            months.forEach(month => {
                const monthItem = document.createElement('li');
                const button = document.createElement('a'); // Use <a> for button role
                button.href = "#"; // Prevent default navigation
                button.textContent = `${year}/${month}`;
                button.dataset.year = year;
                button.dataset.month = month;
                button.setAttribute('role', 'button');
                button.classList.add('outline'); // Pico.css outline button style
                button.addEventListener('click', (event) => {
                    event.preventDefault(); // Prevent page jump
                    loadImages(year, month);
                    setActive(button);
                });
                monthItem.appendChild(button);
                monthList.appendChild(monthItem);
            });

            yearItem.appendChild(monthList);
            navList.appendChild(yearItem);
        });

        navigation.appendChild(navList);
    }

    function loadImages(year, month) {
        charts.innerHTML = '<h1>Sump Water Level</h1>'; // Re-add title as it's cleared
        const images = fileTree[year][month];
        images.forEach(imagePath => {
            const img = document.createElement('img');
            img.src = imagePath;
            charts.appendChild(img);
        });
    }

    function loadLatest() {
        const latestYear = Object.keys(fileTree).sort().reverse()[0];
        const latestMonth = Object.keys(fileTree[latestYear]).sort().reverse()[0];
        loadImages(latestYear, latestMonth);
        const latestMonthItem = navigation.querySelector(`[data-year="${latestYear}"][data-month="${latestMonth}"]`);
        if (latestMonthItem) {
            setActive(latestMonthItem); // Pass the button directly
        }
    }

    function setActive(selectedButton) {
        const currentActive = navigation.querySelector('a.primary');
        if (currentActive) {
            currentActive.classList.remove('primary');
            currentActive.classList.add('outline');
        }
        selectedButton.classList.remove('outline');
        selectedButton.classList.add('primary');
    }
});