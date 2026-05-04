document.addEventListener("DOMContentLoaded", function () {
    const colorChips = document.querySelectorAll(".color-chip");
    const selectedColorText = document.getElementById("selected-color");

    colorChips.forEach(chip => {
        chip.addEventListener("click", function () {
            // remove active from all
            colorChips.forEach(c => c.classList.remove("active"));

            // add active to clicked one
            this.classList.add("active");

            // update text
            const color = this.getAttribute("data-color");
            selectedColorText.textContent = color;
        });
    });
});


function handleAddToWishlist(button) {
    const productId = button.getAttribute("data-productId");
    const userId = button.getAttribute("data-userId");

    if(!userId){
        Swal.fire({
            icon: 'warning',
            title: 'Login Required',
            text: 'Please log in to add items to your wishlist.',
            confirmButtonColor: '#3085d6'
        });
        return;
    }


    fetch("/wishlist/add", {
        method: "POST",
        headers:{
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ productId: productId, userId: userId })
    })
    .then(response => response.json())
    .then(data => {
        if(data.success){
            Swal.fire({
                icon: 'success',
                title: 'Added!',
                text: 'Product added to your wishlist.',
                confirmButtonColor: '#3085d6'
            });

            button.classList.add("text-danger"); // make heart red
        }
        else{
            Swal.fire({
                icon: 'info',
                title: 'Oops!',
                text: 'This product is already in your wishlist.',
                confirmButtonColor: '#3085d6'
            });
        }
    })
    .catch(err => {
        console.error("Error:", err);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'Something went wrong while adding to wishlist.',
            confirmButtonColor: '#d33'
        });
    });
}




function removeFromWishlist(button) {
     const productId = button.getAttribute("data-productId");
        const userId = button.getAttribute("data-userId");
        console.log(productId, userId);

        if(!userId){
            Swal.fire({
                icon: 'warning',
                title: 'Login Required',
                text: 'Please log in to add items to your wishlist.',
                confirmButtonColor: '#3085d6'
            });
            return;
        }

    Swal.fire({
        title: "Are you sure?",
        text: "This product will be removed from your wishlist.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#d33",
        confirmButtonText: "Yes, remove it!"
    }).then((result) => {
        if (result.isConfirmed) {
            fetch("/wishlist/remove", {
                method: "DELETE",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ productId: productId, userId: userId })
            })
            .then(res => res.json())
            .then(data => {
                if(data.success){
                    Swal.fire("Removed!", "Item removed from wishlist.", "success");

                    // Remove the card from the DOM without reload
                    const card = button.closest(".wishlist-card");
                    if(card)
                        card.remove();

                    setTimeout(() => {
                        location.reload();
                    }, 5000);
                }
                else {
                    Swal.fire("Info", data.message || "Item not found.", "info");
                }
            })
            .catch(() => Swal.fire("Error", "Failed to remove item", "error"));
        }
    });
}


let currentCategory = '';
let currentSubcategory = '';

let currentMinPrice = 0;
let currentMaxPrice = 500;

// Update price display and progress
function updatePriceDisplay() {
    $('.min-price').text('$' + currentMinPrice);
    $('.max-price').text('$' + currentMaxPrice);

    const minRange = parseInt($('.min-range').attr('min'));
    const maxRange = parseInt($('.max-range').attr('max'));

    const minPercent = ((currentMinPrice - minRange) / (maxRange - minRange)) * 100;
    const maxPercent = ((currentMaxPrice - minRange) / (maxRange - minRange)) * 100;

    $('.slider-progress').css({
        'left': minPercent + '%',
        'right': (100 - maxPercent) + '%'
    });
}

// Slider events
$('.min-range').on('input', function() {
    let val = parseInt($(this).val());
//    console.log(val);
    if(val > currentMaxPrice)
        val = currentMaxPrice;
    currentMinPrice = val;
    $(this).val(val);
    updatePriceDisplay();
});

$('.max-range').on('input', function() {
    let val = parseInt($(this).val());
//    console.log(val);
    if(val < currentMinPrice)
        val = currentMinPrice;
    currentMaxPrice = val;
    $(this).val(val);
    updatePriceDisplay();
});

// Initialize display
//updatePriceDisplay();




function updateActiveFilters() {
    const activeContainer = $('.active-filters .filter-tags');
    activeContainer.empty();

    const params = new URLSearchParams(window.location.search);
    const category = params.get('category') || currentCategory;
    const subcategory = params.get('subcategory') || currentSubcategory;
    const colors = params.getAll('colors');
    const minPrice = params.get('minPrice');
    const maxPrice = params.get('maxPrice');

    // Category/Subcategory
    if(category && subcategory){
        activeContainer.append(
            `<span class="filter-tag">${category} > ${subcategory}
                <button class="filter-remove" onclick="removeFilter('category')"><i class="bi bi-x"></i></button>
            </span>`
        );
    }

    // Colors
    colors.forEach(color => {
        activeContainer.append(
            `<span class="filter-tag">${color}
                <button class="filter-remove" onclick="removeFilter('colors','${color}')"><i class="bi bi-x"></i></button>
            </span>`
        );
    });

    if(minPrice && maxPrice){
        activeContainer.append(
            `<span class="filter-tag">$${minPrice} - $${maxPrice}
                <button class="filter-remove" onclick="removeFilter('price')"><i class="bi bi-x"></i></button>
            </span>`
        );
    }

    // Add Clear All button if any filter is active
    if((category && subcategory) || colors.length > 0 || (minPrice && maxPrice)){
        if($('#clearAllFilters').length === 0){
            activeContainer.append(
                `<button type="button" class="btn btn-sm btn-outline-danger ms-2" id="clearAllFilters">Clear All</button>`
            );
        }
    }
    else{
        $('#clearAllFilters').remove();
    }
}

function removeFilter(type, value = '') {
    if(type === 'colors'){
        $(`#colorFilterForm input[name="colors"][value="${value}"]`).prop('checked', false);
        filterProducts(currentCategory, currentSubcategory);
    }
    if(type === 'category'){
        currentCategory = '';
        currentSubcategory = '';
        $('.subcategory-link').removeClass('active');
        filterProducts();
    }
    if(type === 'price'){
        // Reset price inputs to default values
        currentMinPrice = 0;
        currentMaxPrice = 500; // or whatever your default max is
        $('.min-price-input').val(currentMinPrice);
        $('.max-price-input').val(currentMaxPrice);
        $('.min-range').val(currentMinPrice);
        $('.max-range').val(currentMaxPrice);

        updatePriceDisplay();

        // Apply filter keeping category/subcategory/colors
        filterProducts(currentCategory, currentSubcategory);
    }
}

// Clear all filters
function clearAllFilters() {
    currentCategory = '';
    currentSubcategory = '';
    $('#colorFilterForm input[name="colors"]').prop('checked', false);
    $('.subcategory-link').removeClass('active');

    // Reset price inputs and sliders
    $('.min-price-input').val(0);
    $('.max-price-input').val(1000);
    $('.min-range').val(0);
    $('.max-range').val(1000);

    window.history.pushState({}, '', '/products');
    updateActiveFilters();
    filterProducts();
}


function filterProducts(category = '', subcategory = '') {
    category = category || currentCategory;
    subcategory = subcategory || currentSubcategory;

    // Get selected colors
    let selectedColors = [];
    $('#colorFilterForm input[name="colors"]:checked').each(function() {
        selectedColors.push($(this).val());
    });;
    console.log("Selected colors:", selectedColors);
    console.log("Category:", category, "Subcategory:", subcategory);

    currentCategory = category;
    currentSubcategory = subcategory;

    // Build query string
    let queryParts = [];
    if(category && category.trim() !== '') {
        queryParts.push('category=' + encodeURIComponent(category));
    }
    if(subcategory && subcategory.trim() !== '') {
        queryParts.push('subcategory=' + encodeURIComponent(subcategory));
    }
    if(selectedColors.length > 0) {
        queryParts.push(selectedColors.map(c => 'colors=' + encodeURIComponent(c)).join('&'));
    }
    if(currentMinPrice != 0){
        queryParts.push('minPrice=' + currentMinPrice);
    }
    if(currentMaxPrice != 500){
        queryParts.push('maxPrice=' + currentMaxPrice);
    }


    const query = queryParts.join('&');
    const url = '/products' + (query ? '?' + query : '');

    // Update URL without reload
    window.history.pushState({}, '', url);

    // Optional: loading spinner
    $('#productList').html('<div class="text-center my-5"><div class="spinner-border text-primary" role="status"></div></div>');

    updateActiveFilters();

    $.ajax({
        url: url,
        type: 'GET',
        dataType: 'html',
        success: function(response) {
        const newProducts = $(response).find('#productList').html();
            $('#productList').html(newProducts);
        },
        error: function(xhr) {
            $('#productList').html('<p class="text-danger text-center mt-4">Failed to load products.</p>');
            console.error(xhr);
        }
    });

}


$(document).ready(function () {
     // On page load: read URL parameters
     const params = new URLSearchParams(window.location.search);
     currentCategory = params.get('category') || '';
     currentSubcategory = params.get('subcategory') || '';
     const colors = params.getAll('colors');

     // Highlight subcategory link if category/subcategory present
     if(currentCategory && currentSubcategory){
         $(`.subcategory-link[data-category="${currentCategory}"][data-subcategory="${currentSubcategory}"]`).addClass('active');
     }

     // Check color checkboxes from URL
     colors.forEach(color => {
         $(`#colorFilterForm input[name="colors"][value="${color}"]`).prop('checked', true);
     });

     // Update active filters on page load
     updateActiveFilters();

     // Apply color filter button
     $('#applyColorFilter').on('click', function (e) {
         e.preventDefault();
         filterProducts();
     });

     // Subcategory click
     $(document).on('click', '.subcategory-link', function() {
         $('.subcategory-link').removeClass('active'); // remove previous highlight
         $(this).addClass('active'); // highlight current
         currentCategory = $(this).data('category');
         currentSubcategory = $(this).data('subcategory');
         filterProducts(currentCategory, currentSubcategory);
     });

     $('#applyPriceFilter').on('click', function() {
        filterProducts(currentCategory, currentSubcategory);
     });


     // Clear all button
     $(document).on('click', '#clearAllFilters', function() {
        clearAllFilters();
     });

 });



function clearColorsFilter() {
    // Uncheck all color checkboxes
    $('#colorFilterForm input[name=colors]').prop('checked', false);

    // Keep category/subcategory
    filterProducts(currentCategory, currentSubcategory);
}









