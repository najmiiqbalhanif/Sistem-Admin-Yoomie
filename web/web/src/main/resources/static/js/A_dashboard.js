// JS Sidebar pada dashboard Admin
document.addEventListener('DOMContentLoaded', function() {
    const dashboardLinks = document.querySelectorAll('.dashboard-link');
    const contentDashboard = document.querySelectorAll('.content-dashboard');

    // Sembunyikan semua konten dulu
    contentDashboard.forEach(content => {
        content.style.display = 'none';
    });

    // Cek query param ?section=..., kalau ada pakai itu sebagai tab awal
    let defaultDashboard = 'dashboard';
    const params = new URLSearchParams(window.location.search);
    const sectionFromUrl = params.get('section');

    if (sectionFromUrl) {
        defaultDashboard = sectionFromUrl; // misal: 'library'
    }

    const defaultLink = document.querySelector(`.dashboard-link[menu-dashboard="${defaultDashboard}"]`);
    const defaultContent = document.querySelector(`.${defaultDashboard}`);

    if (defaultLink) {
        defaultLink.classList.add('active');
    }
    if (defaultContent) {
        defaultContent.style.display = 'block';
    }

    dashboardLinks.forEach(link => {
        link.addEventListener('click', () => {
            const selectedMenu = link.getAttribute('menu-dashboard');

            // Hapus active dari semua link
            dashboardLinks.forEach(link => link.classList.remove('active'));

            // Set active ke link yang diklik
            link.classList.add('active');

            // Sembunyikan semua konten
            contentDashboard.forEach(content => {
                content.style.display = 'none';
            });

            // Tampilkan konten sesuai tab yang dipilih
            const activeContent = document.querySelector(`.${selectedMenu}`);
            if (activeContent) {
                activeContent.style.display = 'block';
            }

            // UPDATE URL TANPA RELOAD: set ?section=namaTab
            const url = new URL(window.location);
            url.searchParams.set('section', selectedMenu);
            window.history.replaceState({}, '', url);
        });
    });
});

// JS untuk button edit product pada library dashboard admin
document.addEventListener('DOMContentLoaded', function () {
    const editButtons = document.querySelectorAll('.button-editProd');

    editButtons.forEach(button => {
        button.addEventListener('click', function () {
            const productId = this.getAttribute('data-id');
            const productPhoto = this.getAttribute('data-photo'); // Pastikan atribut data-photo sudah diisi
            const productName = this.getAttribute('data-name');
            const productBrand = this.getAttribute('data-brand');
            const productCategory = this.getAttribute('data-category');
            const productPrice = this.getAttribute('data-price');
            const productStock = this.getAttribute('data-stock');

            // Isi modal dengan data produk
            document.getElementById('editProductId').value = productId;
            document.getElementById('editProductName').value = productName;
            document.getElementById('editProductBrand').value = productBrand;
            document.getElementById('editProductCategory').value = productCategory;
            document.getElementById('editProductPrice').value = productPrice;
            document.getElementById('editProductStock').value = productStock;

            // Atur pratinjau gambar berdasarkan data-photo
            const previewPhoto = document.getElementById('previewProductPhoto');
            if (productPhoto) {
                previewPhoto.src = productPhoto; // Path URL gambar dari server
                previewPhoto.style.display = 'block'; // Tampilkan pratinjau
            } else {
                previewPhoto.style.display = 'none'; // Sembunyikan jika tidak ada foto
            }

            // Atur URL action pada form
            const form = document.getElementById('editProductForm');
            form.setAttribute('action', '/A_dashboard/editProd/' + productId);
        });
    });

    // Tambahkan event listener untuk preview foto baru jika input file berubah
    const editPhotoInput = document.getElementById('editProductPhoto');
    const previewPhoto = document.getElementById('previewProductPhoto');

    editPhotoInput.addEventListener('change', function (event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                previewPhoto.src = e.target.result; // Gunakan data URL untuk pratinjau
                previewPhoto.style.display = 'block'; // Tampilkan pratinjau
            };
            reader.readAsDataURL(file); // Membaca file
        } else {
            previewPhoto.style.display = 'none'; // Sembunyikan pratinjau jika input kosong
        }
    });
});

document.addEventListener('DOMContentLoaded', function () {
    const cartButtons = document.querySelectorAll('.btn-cart');

    cartButtons.forEach(button => {
        button.addEventListener('click', function () {
            const transactionId = button.getAttribute('data-transaction-id');

            // Tambahkan URL yang sesuai dengan endpoint
            fetch(`/A_dashboard/${transactionId}/paymentItems`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    return response.json(); // Konversi ke JSON
                })
                .then(paymentItems => {
                    // Kosongkan tabel sebelum menambahkan data baru
                    const paymentItemsTable = document.getElementById('paymentItemsTable');
                    paymentItemsTable.innerHTML = '';

                    // Tambahkan item ke tabel
                    paymentItems.forEach(item => {
                        const row = `
                            <tr>
                                <td>${item.productName}</td>
                                <td>${item.quantity}</td>
                                <td>${item.price}</td>
                                <td>${item.subTotal}</td>
                            </tr>
                        `;
                        paymentItemsTable.insertAdjacentHTML('beforeend', row);
                    });
                })
                .catch(error => console.error('Error fetching payment items:', error));
        });
    });
});

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.status-dropdown').forEach(dropdown => {
        dropdown.addEventListener('change', function () {
            const transactionId = this.getAttribute('data-transaction-id');
            const newStatus = this.value;

            fetch(`/A_dashboard/updateStatus/${transactionId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status: newStatus })
            })
                .then(response => {
                    if (response.ok) {
                        alert('Status updated successfully!');
                    } else {
                        alert('Failed to update status.');
                    }
                })
                .catch(error => console.error('Error:', error));
        });
    });
});

document.addEventListener('DOMContentLoaded', function () {
    // Toggle dropdown user di topbar
    const userToggle = document.querySelector('.topbar-user');

    if (userToggle) {
        const userMenu = userToggle.querySelector('.topbar-user-menu');

        // klik avatar/nama → buka/tutup
        userToggle.addEventListener('click', function (e) {
            e.stopPropagation();
            userToggle.classList.toggle('open');
        });

        // biar klik di dalam menu gak nutup
        if (userMenu) {
            userMenu.addEventListener('click', function (e) {
                e.stopPropagation();
            });
        }

        // klik di luar → tutup
        document.addEventListener('click', function () {
            userToggle.classList.remove('open');
        });
    }
});

document.addEventListener('DOMContentLoaded', function () {

    // SEARCH LIBRARY
    const searchInput = document.getElementById('librarySearchInput');
    const libraryTable = document.getElementById('libraryTable');

    if (searchInput && libraryTable) {
        const rows = libraryTable.querySelectorAll('tbody tr');

        searchInput.addEventListener('input', function () {
            const query = this.value.trim().toLowerCase();

            rows.forEach(function (row) {
                const nameCell = row.querySelector('td:nth-child(1)'); // kolom Name
                if (!nameCell) return;

                const nameText = nameCell.textContent.toLowerCase();

                // contains match (substring, case-insensitive)
                if (query === '' || nameText.includes(query)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const stockLogButtons = document.querySelectorAll('.button-stockLogs');

    stockLogButtons.forEach(button => {
        button.addEventListener('click', function () {
            const productId = button.getAttribute('data-product-id');
            const productName = button.getAttribute('data-product-name');

            const nameEl = document.getElementById('stockLogProductName');
            if (nameEl) nameEl.textContent = productName || '-';

            const tbody = document.getElementById('stockLogTableBody');
            const errBox = document.getElementById('stockLogError');

            if (errBox) {
                errBox.classList.add('d-none');
                errBox.textContent = '';
            }
            if (tbody) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" class="text-muted small text-center">Loading...</td>
                    </tr>
                `;
            }

            fetch(`/A_dashboard/stockLogs/${productId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(logs => {
                    if (!tbody) return;

                    if (!logs || logs.length === 0) {
                        tbody.innerHTML = `
                            <tr>
                                <td colspan="5" class="text-muted small text-center">Belum ada log stok.</td>
                            </tr>
                        `;
                        return;
                    }

                    tbody.innerHTML = '';

                    logs.forEach(log => {
                        const createdOn = log.createdOn || log.createdAt || '-';
                        const oldStock = (log.oldStock ?? '-');
                        const newStock = (log.newStock ?? '-');
                        const diff = (log.diff ?? (typeof oldStock === 'number' && typeof newStock === 'number' ? (newStock - oldStock) : '-'));

                        const diffBadge = (typeof diff === 'number')
                            ? (diff >= 0
                                ? `<span class="badge bg-success">+${diff}</span>`
                                : `<span class="badge bg-danger">${diff}</span>`)
                            : `<span class="badge bg-secondary">${diff}</span>`;

                        const row = `
                            <tr>
                                <td>${createdOn}</td>
                                <td>${oldStock}</td>
                                <td>${newStock}</td>
                                <td>${diffBadge}</td>
                            </tr>
                        `;
                        tbody.insertAdjacentHTML('beforeend', row);
                    });
                })
                .catch(error => {
                    console.error('Error fetching stock logs:', error);

                    if (tbody) {
                        tbody.innerHTML = `
                            <tr>
                                <td colspan="5" class="text-muted small text-center">Tidak bisa memuat data.</td>
                            </tr>
                        `;
                    }

                    if (errBox) {
                        errBox.textContent = error.message || 'Terjadi kesalahan';
                        errBox.classList.remove('d-none');
                    }
                });
        });
    });
});
