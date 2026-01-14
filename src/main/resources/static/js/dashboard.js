let jobId = null;
let progress = 0;
let timer = null;

function startMigration() {
    const source = document.getElementById("source").value;
    const target = document.getElementById("target").value;

    document.getElementById("status").innerText = "Running...";
    progress = 0;
    updateProgress();

    fetch(`/api/migration/start?sourceId=${source}&targetId=${target}`, {
        method: "POST"
    }).catch(showError);

    timer = setInterval(fetchLogs, 2000);
}

function fetchLogs() {
    if (!jobId) jobId = 1; // single job demo (safe default)

    fetch(`/api/migration/logs/${jobId}`)
        .then(r => r.json())
        .then(data => {
            let logText = "";
            data.forEach(l => logText += l.logTime + " - " + l.message + "\n");
            document.getElementById("logs").innerText = logText;

            progress = Math.min(100, progress + 10);
            updateProgress();
        })
        .catch(showError);
}

function updateProgress() {
    document.getElementById("progress").style.width = progress + "%";
    if (progress === 100) {
        document.getElementById("status").innerText = "Completed";
        clearInterval(timer);
    }
}

function showError(e) {
    document.getElementById("errorText").innerText = e;
    document.getElementById("errorPopup").classList.remove("hidden");
}

function closePopup() {
    document.getElementById("errorPopup").classList.add("hidden");
}
