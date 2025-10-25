//JS Sidebar pada dashboard Admin
document.addEventListener('DOMContentLoaded', function() {
    const dashboardLinks = document.querySelectorAll('.dashboard-link')
    const contentDashboard = document.querySelectorAll('.content-dashboard')

    contentDashboard.forEach(content => {
        content.style.display = 'none';
    });

    const defaultDashboard = 'dashboard';

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

            dashboardLinks.forEach(link => link.classList.remove('active'))

            link.classList.add('active');

            contentDashboard.forEach(content => {
                content.style.display = 'none'
            });

            const activeContent = document.querySelector(`.${selectedMenu}`)
            if (activeContent) {
                activeContent.style.display = 'block'
            }
        })
    });
})

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
            const orderId = button.getAttribute('data-order-id');

            // Tambahkan URL yang sesuai dengan endpoint
            fetch(`/A_dashboard/${orderId}/paymentItems`)
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
            const orderId = this.getAttribute('data-order-id');
            const newStatus = this.value;

            fetch(`/A_dashboard/updateStatus/${orderId}`, {
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