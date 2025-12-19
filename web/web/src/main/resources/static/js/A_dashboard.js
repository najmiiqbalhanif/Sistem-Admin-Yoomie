document.addEventListener('DOMContentLoaded', function () {
    // Ini BUKAN i18n bundle, hanya konstanta teks untuk JS
    const I18N = {
        loading: 'Loading...',
        noStockLogs: 'No stock logs yet.',
        cannotLoad: 'Unable to load data.',
        errorGeneric: 'An error occurred.',
        statusUpdated: 'Status updated successfully!',
        statusUpdateFailed: 'Failed to update status.'
    };

    // ========= 1) SIDEBAR TAB NAVIGATION (keeps ?section only) =========
    const dashboardLinks = document.querySelectorAll('.dashboard-link');
    const contentDashboard = document.querySelectorAll('.content-dashboard');

    function hideAllSections() {
        contentDashboard.forEach(content => (content.style.display = 'none'));
        dashboardLinks.forEach(link => link.classList.remove('active'));
    }

    function showSection(sectionName) {
        hideAllSections();

        const link = document.querySelector(`.dashboard-link[menu-dashboard="${sectionName}"]`);
        const content = document.querySelector(`.${sectionName}`);

        if (link) link.classList.add('active');
        if (content) content.style.display = 'block';

        // Update URL without reload, keep other params (kecuali tidak ada lang)
        const url = new URL(window.location.href);
        url.searchParams.set('section', sectionName);
        window.history.replaceState({}, '', url);
    }

    // init
    hideAllSections();
    const params = new URLSearchParams(window.location.search);
    const sectionFromUrl = params.get('section') || 'dashboard';
    showSection(sectionFromUrl);

    dashboardLinks.forEach(link => {
        link.addEventListener('click', () => {
            const selectedMenu = link.getAttribute('menu-dashboard');
            showSection(selectedMenu);
        });
    });

    // ========= 2) TOPBAR USER DROPDOWN =========
    const userToggle = document.getElementById('topbarUser');
    if (userToggle) {
        const userMenu = userToggle.querySelector('.topbar-user-menu');

        userToggle.addEventListener('click', function (e) {
            e.stopPropagation();
            userToggle.classList.toggle('open');
        });

        if (userMenu) {
            userMenu.addEventListener('click', function (e) {
                e.stopPropagation();
            });
        }

        document.addEventListener('click', function () {
            userToggle.classList.remove('open');
        });
    }

    // ========= 3) (REMOVED) LANGUAGE SWITCH =========
    // Karena non-i18n, bagian ini dihapus.

    // ========= 4) CONFIRM SUBMIT (Delete forms) =========
    document.querySelectorAll('form.form-confirm').forEach(form => {
        form.addEventListener('submit', (e) => {
            const msg = form.dataset.confirm || 'Are you sure?';
            if (!confirm(msg)) e.preventDefault();
        });
    });

    // ========= 5) EDIT PRODUCT MODAL FILL + PREVIEW =========
    const editButtons = document.querySelectorAll('.button-editProd');

    editButtons.forEach(button => {
        button.addEventListener('click', function () {
            const productId = this.getAttribute('data-id');
            const productPhoto = this.getAttribute('data-photo');
            const productName = this.getAttribute('data-name');
            const productBrand = this.getAttribute('data-brand');
            const productCategory = this.getAttribute('data-category');
            const productPrice = this.getAttribute('data-price');
            const productStock = this.getAttribute('data-stock');

            const elId = document.getElementById('editProductId');
            const elName = document.getElementById('editProductName');
            const elBrand = document.getElementById('editProductBrand');
            const elCategory = document.getElementById('editProductCategory');
            const elPrice = document.getElementById('editProductPrice');
            const elStock = document.getElementById('editProductStock');

            if (elId) elId.value = productId || '';
            if (elName) elName.value = productName || '';
            if (elBrand) elBrand.value = productBrand || '';
            if (elCategory) elCategory.value = productCategory || '';
            if (elPrice) elPrice.value = productPrice || '';
            if (elStock) elStock.value = productStock || '';

            const previewPhoto = document.getElementById('previewProductPhoto');
            if (previewPhoto) {
                if (productPhoto) {
                    previewPhoto.src = productPhoto;
                    previewPhoto.style.display = 'block';
                } else {
                    previewPhoto.style.display = 'none';
                    previewPhoto.src = '';
                }
            }

            const form = document.getElementById('editProductForm');
            if (form && productId) {
                form.setAttribute('action', '/A_dashboard/editProd/' + productId);
            }
        });
    });

    // ==== PREVIEW FOTO (ADD + EDIT) ====
    function initImagePreview(inputId, imgId) {
        const input = document.getElementById(inputId);
        const img = document.getElementById(imgId);
        if (!input || !img) return;

        input.addEventListener('change', function (event) {
            const file = event.target.files && event.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    img.src = e.target.result;
                    img.style.display = 'block';
                };
                reader.readAsDataURL(file);
            } else {
                img.style.display = 'none';
                img.src = '';
            }
        });
    }

    // preview untuk ADD NEW PRODUCT
    initImagePreview('productPhoto', 'previewNewProductPhoto');

    // preview untuk EDIT PRODUCT (saat user ganti foto)
    initImagePreview('editProductPhoto', 'previewProductPhoto');

    // ========= 6) CART MODAL: Fetch payment items =========
    document.querySelectorAll('.btn-cart').forEach(button => {
        button.addEventListener('click', function () {
            const transactionId = button.getAttribute('data-transaction-id');
            if (!transactionId) return;

            fetch(`/A_dashboard/${transactionId}/paymentItems`)
                .then(response => {
                    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                    return response.json();
                })
                .then(paymentItems => {
                    const paymentItemsTable = document.getElementById('paymentItemsTable');
                    if (!paymentItemsTable) return;

                    paymentItemsTable.innerHTML = '';

                    (paymentItems || []).forEach(item => {
                        const row = `
              <tr>
                <td>${item.productName ?? ''}</td>
                <td>${item.quantity ?? ''}</td>
                <td>${item.price ?? ''}</td>
                <td>${item.subTotal ?? ''}</td>
              </tr>
            `;
                        paymentItemsTable.insertAdjacentHTML('beforeend', row);
                    });
                })
                .catch(error => console.error('Error fetching payment items:', error));
        });
    });

    // ========= 7) STATUS DROPDOWN (if exists) =========
    document.querySelectorAll('.status-dropdown').forEach(dropdown => {
        dropdown.addEventListener('change', function () {
            const transactionId = this.getAttribute('data-transaction-id');
            const newStatus = this.value;

            if (!transactionId) return;

            fetch(`/A_dashboard/updateStatus/${transactionId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status: newStatus })
            })
                .then(response => {
                    if (response.ok) alert(I18N.statusUpdated);
                    else alert(I18N.statusUpdateFailed);
                })
                .catch(error => console.error('Error:', error));
        });
    });

    // ========= 8) SEARCH LIBRARY =========
    const searchInput = document.getElementById('librarySearchInput');
    const libraryTable = document.getElementById('libraryTable');
    if (searchInput && libraryTable) {
        searchInput.addEventListener('input', function () {
            const query = this.value.trim().toLowerCase();
            const rows = libraryTable.querySelectorAll('tbody tr');

            rows.forEach(row => {
                const nameCell = row.querySelector('td:nth-child(1)');
                if (!nameCell) return;

                const nameText = (nameCell.textContent || '').toLowerCase();
                row.style.display = (query === '' || nameText.includes(query)) ? '' : 'none';
            });
        });
    }

    // ========= 9) STOCK LOGS MODAL =========
    document.querySelectorAll('.button-stockLogs').forEach(button => {
        button.addEventListener('click', function () {
            const productId = button.getAttribute('data-product-id');
            const productName = button.getAttribute('data-product-name');

            if (!productId) return;

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
            <td colspan="4" class="text-muted small text-center">${I18N.loading}</td>
          </tr>
        `;
            }

            fetch(`/A_dashboard/stockLogs/${productId}`)
                .then(response => {
                    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                    return response.json();
                })
                .then(logs => {
                    if (!tbody) return;

                    if (!logs || logs.length === 0) {
                        tbody.innerHTML = `
              <tr>
                <td colspan="4" class="text-muted small text-center">${I18N.noStockLogs}</td>
              </tr>
            `;
                        return;
                    }

                    tbody.innerHTML = '';

                    logs.forEach(log => {
                        const createdOn = log.createdOn || log.createdAt || '-';
                        const oldStock = (log.oldStock ?? '-');
                        const newStock = (log.newStock ?? '-');

                        const diff = (log.diff ?? (typeof oldStock === 'number' && typeof newStock === 'number'
                            ? (newStock - oldStock)
                            : '-'));

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
                <td colspan="4" class="text-muted small text-center">${I18N.cannotLoad}</td>
              </tr>
            `;
                    }

                    if (errBox) {
                        errBox.textContent = error.message || I18N.errorGeneric;
                        errBox.classList.remove('d-none');
                    }
                });
        });
    });
});
