function selectSize(button) {
    // Hapus kelas aktif dari semua tombol
    const sizeButtons = document.querySelectorAll('.size-prod');
    sizeButtons.forEach(btn => btn.classList.remove('active'));

    // Tambahkan kelas aktif ke tombol yang dipilih
    button.classList.add('active');

    // Aktifkan tombol "Add to Cart" hanya jika ukuran dipilih
    const addToCartBtn = document.getElementById('addToCartBtn');
    addToCartBtn.disabled = false;
}
