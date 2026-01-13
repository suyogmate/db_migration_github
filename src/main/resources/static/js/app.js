function openPopup() {
    popup.style.display = "block";
}

function closePopup() {
    popup.style.display = "none";
}

function saveDb() {
    fetch('/api/databases', {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({
            name: name.value,
            dbType: dbType.value,
            host: host.value,
            port: port.value,
            dbName: dbName.value,
            schemaName: schemaName.value,
            username: username.value,
            password: password.value
        })
    }).then(() => location.reload());
}

function startMigration() {
    const src = document.getElementById("sourceDb").value;
    const tgt = document.getElementById("targetDb").value;

    if (!src || !tgt) {
        alert("Select both source and target");
        return;
    }

    fetch(`/api/migration/start?sourceDbId=${src}&targetDbId=${tgt}`, { method: 'POST' })
        .then(r => r.text())
        .then(msg => {
            simulateProgress();
            alert(msg);
        });
}

function simulateProgress() {
    let bar = document.getElementById("progressBar");
    let width = 0;
    let timer = setInterval(() => {
        width += 10;
        bar.style.width = width + "%";
        if (width >= 100) clearInterval(timer);
    }, 500);
}
