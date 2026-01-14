let logInterval = null;

function openPopup() {
    document.getElementById("popup").style.display = "block";
}

function closePopup() {
    document.getElementById("popup").style.display = "none";
}

function viewLogs(jobId) {

    if (logInterval) clearInterval(logInterval);

    fetchLogs(jobId);

    logInterval = setInterval(() => {
        fetchLogs(jobId);
    }, 2000);
}

function fetchLogs(jobId) {
    fetch(`/logs/${jobId}`)
        .then(res => res.json())
        .then(data => {
            let box = document.getElementById("logBox");
            box.textContent = data.map(l => l.message).join("\n");
        });
}
