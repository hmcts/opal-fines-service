function copyTestSummary(tableId) {
    const table = document.getElementById(tableId);
    if (!table) return;

    const rows = Array.from(table.querySelectorAll("tr")).slice(1); // Skip header
    const lines = rows.map(row => {
        const cells = row.querySelectorAll("td");
        return cells[0].innerText + "\t" + cells[1].innerText;
    });

    const text = lines.join("\n");

    navigator.clipboard.writeText(text)
        .then(() => alert("Test Suite + Total copied to clipboard!"))
        .catch(err => alert("Failed to copy: " + err));
}
                function copyTotalsOnly(tableId) {
                    const table = document.getElementById(tableId);
                    if (!table) return;

                    const rows = Array.from(table.querySelectorAll("tr")).slice(1); // Skip header
                    const lines = rows.map(row => {
                        const cells = row.querySelectorAll("td");
                        return cells[1]?.innerText || '';
                    });

                    const text = lines.join("\n");

                    navigator.clipboard.writeText(text)
                        .then(() => alert("Total column copied to clipboard!"))
                        .catch(err => alert("Failed to copy: " + err));
                }
