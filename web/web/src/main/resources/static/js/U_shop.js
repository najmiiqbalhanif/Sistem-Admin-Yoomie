document.addEventListener('DOMContentLoaded', function() {
    const categoryLinks = document.querySelectorAll('.category-link')
    const contentCategories = document.querySelectorAll('.content-category')

    const defaultCategory = 'shoes';

    const defaultLink = document.querySelector(`.category-link[data-category="${defaultCategory}"]`);
    const defaultContent = document.querySelector(`.${defaultCategory}`);

    if (defaultLink) {
        defaultLink.classList.add('active');
    }
    if (defaultContent) {
        defaultContent.style.display = 'block';
    }

    categoryLinks.forEach(link => {
        link.addEventListener('click', () => {
            const selectedCategory = link.getAttribute('data-category')

            categoryLinks.forEach(link => link.classList.remove('active'))

            link.classList.add('active');

            contentCategories.forEach(content => {
                content.style.display = 'none'
            });

            const activeContent = document.querySelector(`.${selectedCategory}`)
            if (activeContent) {
                activeContent.style.display = 'block'
            }
        })
    });
})