document.addEventListener("DOMContentLoaded", function () {
    const dashboardTitle = document.querySelector(".dashboard-title");
    const sections = document.getElementById("sections");

    // Tampilkan sections saat kursor berada di dashboard-title
    dashboardTitle.addEventListener("mouseenter", () => {
        sections.classList.remove("hidden");
        sections.style.opacity = "1";
        sections.style.maxHeight = "500px";
    });

    // Sembunyikan sections saat kursor keluar dari sections
    sections.addEventListener("mouseleave", () => {
        sections.classList.add("hidden");
        sections.style.opacity = "0";
        sections.style.maxHeight = "0";
    });
});

document.getElementById('uploadPhotoBtn').addEventListener('click', function (e) {
    e.preventDefault();
    document.getElementById('photoInput').click();
});

document.getElementById('photoInput').addEventListener('change', function () {
    const formData = new FormData();
    formData.append('photo', this.files[0]);

    fetch('/upload-photo', { // Sesuaikan endpoint ini
        method: 'POST',
        body: formData,
        headers: {
            'X-CSRF-TOKEN': document.querySelector('meta[name="csrf-token"]').content,
        },
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('Photo uploaded successfully!');
            } else {
                alert('Photo upload failed!');
            }
        })
        .catch(error => {
            console.error('Error uploading photo:', error);
        });
});

// JavaScript for Don't Miss Section
document.getElementById('dontMissUploadPhotoBtn').addEventListener('click', function (e) {
    e.preventDefault();
    document.getElementById('dontMissPhotoInput').click();
});

document.getElementById('dontMissPhotoInput').addEventListener('change', function () {
    const formData = new FormData();
    formData.append('photo', this.files[0]);

    fetch('/upload-dontmiss-photo', { // Sesuaikan endpoint ini
        method: 'POST',
        body: formData,
        headers: {
            'X-CSRF-TOKEN': document.querySelector('meta[name="csrf-token"]').content,
        },
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('Photo uploaded successfully for Don\'t Miss section!');
            } else {
                alert('Photo upload failed for Don\'t Miss section!');
            }
        })
        .catch(error => {
            console.error('Error uploading photo for Don\'t Miss section:', error);
        });
});
