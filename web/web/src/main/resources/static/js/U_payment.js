// Tampilkan formulir Mastercard saat halaman dimuat
$(document).ready(function () {
    // Tampilkan form Mastercard dan sembunyikan form lainnya
    $('#mastercardForm').show();
    $('#transferForm, #danaForm').hide();

    // Tambahkan style aktif pada Mastercard
    $('#mastercardOption').addClass('active');

    // Event listener untuk klik pada Mastercard
    $('#mastercardOption').on('click', function () {
        $('#mastercardForm').show();
        $('#transferForm, #danaForm').hide();

        // Highlight pilihan aktif
        $('#mastercardOption').addClass('active');
        $('#transferOption, #danaOption').removeClass('active');
    });

    // Event listener untuk klik pada Transfer
    $('#transferOption').on('click', function () {
        $('#transferForm').show();
        $('#mastercardForm, #danaForm').hide();

        // Highlight pilihan aktif
        $('#transferOption').addClass('active');
        $('#mastercardOption, #danaOption').removeClass('active');
    });

    // Event listener untuk klik pada Dana
    $('#danaOption').on('click', function () {
        $('#danaForm').show();
        $('#mastercardForm, #transferForm').hide();

        // Highlight pilihan aktif
        $('#danaOption').addClass('active');
        $('#mastercardOption, #transferOption').removeClass('active');
    });


// Function to calculate and update Duties & Taxes and Total
    function calculateDutiesAndTotal() {
        // Get the Subtotal value from the DOM (which was rendered by Thymeleaf)
        const subtotalValueElement = document.getElementById('subtotalValue');
        let subtotal = parseFloat(subtotalValueElement.textContent.replace('Rp ', '').replace(',', '').replace('.', ''));

        // Calculate 12% Duties & Taxes
        let dutiesAndTaxes = subtotal * 0.12;

        // Update the Duties & Taxes in the DOM
        const dutiesValueElement = document.getElementById('dutiesValue');
        dutiesValueElement.textContent = 'Rp ' + dutiesAndTaxes.toLocaleString();

        // Calculate Total (Subtotal + Duties & Taxes)
        let total = subtotal + dutiesAndTaxes;

        // Update the Total in the DOM
        const totalValueElement = document.getElementById('totalValue');
        totalValueElement.textContent = 'Rp ' + total.toLocaleString();
    }

// Call the function when the page is loaded
    window.onload = calculateDutiesAndTotal;
});

// Event listener untuk Mastercard
$("#mastercardOption").on("click", function () {
    $("#paymentMethod").val("mastercard");
});

// Event listener untuk Transfer
$("#transferOption").on("click", function () {
    $("#paymentMethod").val("transfer");
});

// Event listener untuk Dana
$("#danaOption").on("click", function () {
    $("#paymentMethod").val("dana");
});
