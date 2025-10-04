$(document).ready(function () {
    $("input[name='shipping']").on("change", function () {
        let shippingPrice = $(this).val();   // "4.99", "12.99", or "0"
        let userId = $("#userId").val();

        console.log("Selected shipping:" + shippingPrice + " for user " + userId);

        $.ajax({
            url: "/user/cartSummaryAjax",
            method: "GET",
            dataType: "json",
            data: {
                uid: userId,
                shipping: shippingPrice
            },
            success: function (response) {
                console.log("Server response:", response);
                if (response.success) {
                    $("#cart-shipping").text("₹" + response.shipping);
                    $("#cart-subtotal").text("₹" + response.subtotal);
                    $("#cart-tax").text("₹" + response.tax);
                    $("#cart-total").text("₹" + response.totalOrderPrice);
                }
            },
            error: function (xhr, status, error) {
                console.error("AJAX error:", error);
                alert("Error updating shipping. Please try again.");
            }
        });
    });
});







function getSelectedShipping() {
    let shippingInput = document.querySelector("input[name='shipping']:checked");
    return shippingInput ? shippingInput.id : null;
}


function addItemCart(productId, userId, color, size) {
    let shippingMethod = getSelectedShipping();

    console.log("Shipping Method:", shippingMethod);

    fetch(`/user/cartSummaryAjax?uid=${userId}&pid=${productId}&action=add&shipping=${shippingMethod}&color=${encodeURIComponent(color)}&size=${size}`)
        .then(res => res.json())
        .then(data => {
            if(data.success){
                Swal.fire({
                    title: "Product added!",
                    text: "Check your cart summary below.",
                    icon: "success"
                });

                 // ✅ Update navbar cart count
                 let cartCountEl = document.getElementById("navbar-cart-count");
                 if(cartCountEl && data.cartCount !== undefined) {
                     cartCountEl.textContent = data.cartCount;
                 }

                 console.log(document.getElementById("cart-summary"));

                // ✅ If we are on cart page, update immediately
                if(document.getElementById("cart-summary")){
                     updateSummaryUI(data);

                      // Directly update DOM with new values
                      if(document.getElementById("cart-subtotal")) {
                           document.getElementById("cart-subtotal").textContent = "₹ " + data.subtotal.toFixed(2);
                      }
                      if(document.getElementById("cart-tax")) {
                           document.getElementById("cart-tax").textContent = "₹ " + data.tax.toFixed(2);
                      }
                      if(document.getElementById("cart-total")) {
                           document.getElementById("cart-total").textContent = "₹ " + data.total.toFixed(2);
                      }
                }
            }
        })
    .catch(err => console.error("Add to cart failed:", err));
}

function handleAddToCart(button) {
        // Read values from data attributes
    const stock = parseInt(button.getAttribute("data-stock"));
    const isLoggedIn = button.getAttribute("data-loggedIn") === "true";
    const productId = button.getAttribute("data-productId");
    const userId = button.getAttribute("data-userId");

    const selectedColorElement = document.getElementById("selected-color");
    let selectedColor=selectedColorElement?selectedColorElement.textContent.trim():null;

    console.log(stock, productId, userId, selectedColor);

    // ✅ If no color selected, set default
    if(!selectedColor || selectedColor === ""){
        selectedColor = "Midnight Black";
    }

    const sizeDropdown = document.getElementById("sizeDropdown");
    let selectedSize = sizeDropdown ? sizeDropdown.value : "M";

    // ✅ If no size selected, set default
    if(!selectedSize || selectedSize === ""){
       selectedSize = "M";
    }


    console.log(stock, isLoggedIn, productId, userId, "Color:", selectedColor, selectedSize);

    if(!isLoggedIn){
        Swal.fire({
            icon: 'warning',
            title: 'Login Required',
            text: 'Please login to add items to cart',
            confirmButtonText: 'Login',
            showCancelButton: true
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = '/signin'; // redirect to login
            }
        });

        return;
    }

    if (stock <= 0) {
        Swal.fire({
            icon: 'error',
            title: 'Out of Stock',
            text: 'Sorry, this product is currently unavailable.',
        });

        return;
    }

    // ✅ if everything is fine → call your cart function
    addItemCart(productId, userId, selectedColor, selectedSize);
}



function addAllToCart(userId) {
    if(!userId) {
        Swal.fire({
            icon: 'warning',
            title: 'Login Required',
            text: 'Please log in to add items to your cart.',
        });
        return;
    }

    // Get currently selected shipping method
    let shippingMethod = getSelectedShipping() || "standard"; // fallback to standard if none selected

    const wishlistCards = document.querySelectorAll(".wishlist-card");
    let addedCount = 0;
    let skippedCount = 0;
    let skippedProducts = [];

    wishlistCards.forEach(card => {
        const addCartBtn = card.querySelector(".btn-add-cart");
        if (!addCartBtn) return;

        const stock = parseInt(addCartBtn.getAttribute("data-stock") || 0);
        const productId = addCartBtn.getAttribute("data-productId");
        const color = document.getElementById("selected-color")?.textContent.trim() || "Midnight Black";
        const size = document.getElementById("sizeDropdown")?.value || "M";

        console.log(stock, productId, color, size);

        if (stock > 0 && productId) {
            // Add to cart
            fetch(`/user/cartSummaryAjax?uid=${userId}&pid=${productId}&action=add&shipping=${shippingMethod}&color=${encodeURIComponent(color)}&size=${encodeURIComponent(size)}`)
                .then(res => res.json())
                .then(data => {
                    if(data.success){
                        // Update navbar count
                        const cartCountEl = document.getElementById("navbar-cart-count");
                        if(cartCountEl && data.cartCount !== undefined) {
                            cartCountEl.textContent = data.cartCount;
                        }
                    }
                })
                .catch(err => console.error("Add to cart failed:", err));

            addedCount++;
        }
        else if (productId) {
            skippedCount++;
            skippedProducts.push(card.querySelector("h4 span")?.textContent || "Unnamed Product");
        }
    });

    let message = `Added ${addedCount} product(s) to your cart.`;
    if(skippedCount > 0) {
        message += ` Skipped ${skippedCount} out-of-stock product(s).`;
    }

    Swal.fire({
        icon: 'success',
        title: 'Wishlist Processed!',
        html: `<p>${message}</p>${skippedProducts.length ? '<p>Skipped: ' + skippedProducts.join(", ") + '</p>' : ''}`,
        confirmButtonColor: '#3085d6'
    });
}







function updateCart(button, action) {
    let cartItem = button.closest(".cart-item");
    if(!cartItem) {
        console.error("❌ No cart item found for button:", button);
        return;
    }

    let productId = cartItem.getAttribute("data-productId");
    let userId = cartItem.getAttribute("data-userId");
    let shippingMethod = getSelectedShipping();

    console.log(productId + " " + userId);
    console.log(shippingMethod);

    if(!userId || !productId) {
        console.error("❌ Missing userId or productId", { userId, productId });
        return;
    }

    // ✅ Show confirmation only for remove
    if(action === 'remove') {
        Swal.fire({
            title: "Are you sure?",
            text: "This item will be removed from your cart.",
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#d33",
            cancelButtonColor: "#3085d6",
            confirmButtonText: "Yes, remove it!",
            cancelButtonText: "Cancel"
        })
        .then((result) => {
            if(result.isConfirmed) {
                performCartUpdate(cartItem, userId, productId, action, shippingMethod, true);
            }
        });
    }
    else {
        // directly update for increase/decrease
        performCartUpdate(cartItem, userId, productId, action, shippingMethod, false);
    }
}

function performCartUpdate(cartItem, userId, productId, action, shippingMethod, allowUndo) {
    let url = `/user/cartSummaryAjax?uid=${userId}&pid=${productId}&action=${action}`;
        if (shippingMethod) {
            url += `&shipping=${shippingMethod}`;
        }

        fetch(url)
        .then(res => res.json())
        .then(data => {
            if(data.success) {
                // ✅ Always update navbar cart count
                let cartCountEl = document.getElementById("navbar-cart-count");
                if(cartCountEl && data.cartCount !== undefined) {
                    cartCountEl.textContent = data.cartCount;
                }


                if(action === 'remove' || data.newQuantity === 0) {
                    cartItem.remove();

                    if(document.querySelectorAll(".cart-item").length === 0){
                        location.reload();
                    }

                    if(allowUndo) {
                        Swal.fire({
                            title: "Removed!",
                            text: "The item has been removed. Undo?",
                            icon: "success",
                            showCancelButton: true,
                            confirmButtonText: "Undo",
                            cancelButtonText: "Close",
                            timer: 5000, // auto close after 5s
                            timerProgressBar: true
                        })
                        .then((result) => {
                            if(result.isConfirmed) {
                                // ✅ Restore item
                                fetch(`/user/cartSummaryAjax?uid=${userId}&pid=${productId}&action=add&shipping=${shippingMethod}`)
                                    .then(res => res.json())
                                    .then(restoreData => {
                                        if(restoreData.success) {
                                            Swal.fire({
                                                title: "Restored!",
                                                text: "The item has been added back to your cart.",
                                                icon: "success",
                                                timer: 1500,
                                                showConfirmButton: false
                                            });
                                            // refresh cart UI after restore
                                            location.reload();
                                        }
                                    });
                            }
                        });
                    }
                }
                else {
                    // update quantity & subtotal
                    if (data.newQuantity !== undefined) {
                        //cartItem.querySelector(".quantity-input").value = data.newQuantity;
                         const qtyInput = cartItem.querySelector(".quantity-input");
                         if(qtyInput) {
                            qtyInput.value = data.newQuantity;
                         }
                    }
                    if (data.itemSubtotal !== undefined) {
                        //cartItem.closest(".row").querySelector(".subtotal-value").textContent = "₹ " + data.itemSubtotal;
                         const subtotalEl = cartItem.querySelector(".subtotal-value");
                         if(subtotalEl){
                            subtotalEl.textContent = "₹ " + data.itemSubtotal;
                         }
                    }
                }

                updateSummaryUI(data);
            }
        })
        .catch(err => console.error("❌ Update cart failed:", err));
}


function updateSummaryUI(data) {
    if (document.getElementById("cart-subtotal")) {
            document.getElementById("cart-subtotal").textContent = "₹ " + data.subtotal;
        }
        if (document.getElementById("cart-tax")) {
            document.getElementById("cart-tax").textContent = "₹ " + data.tax;
        }
        if (document.getElementById("cart-shipping")) {
            document.getElementById("cart-shipping").textContent = "₹ " + data.shipping;
        }
        if (document.getElementById("cart-total")) {
            // ✅ total already includes subtotal + shipping + tax
            document.getElementById("cart-total").textContent = "₹ " + data.totalOrderPrice;
        }

        // ✅ Enforce Free Shipping if subtotal >= 300
        const subtotal = parseFloat(data.subtotal);
        let freeRadio = document.getElementById("free");
        let freeLabel = document.querySelector("label[for='free']");
        let standardRadio = document.getElementById("standard");
        let expressRadio = document.getElementById("express");

        if (subtotal >= 300) {
            if (freeRadio) freeRadio.checked = true;
            if (standardRadio) standardRadio.disabled = true;
            if (expressRadio) expressRadio.disabled = true;

            // ✅ apply CSS class instead of changing text
            if (freeLabel)
                freeLabel.classList.add("applied");

            // make sure backend recalculates with free shipping
            const userId = document.querySelector(".cart-item")?.getAttribute("data-userId");
            fetch(`/user/cartSummaryAjax?uid=${userId}&shipping=free`)
                .then(res => res.json())
                .then(freeData => {
                    if (freeData.success) {
                        document.getElementById("cart-shipping").textContent = "₹ " + freeData.shipping;
                        document.getElementById("cart-total").textContent = "₹ " + freeData.totalOrderPrice;
                    }
                });
        }
        else {
            if(standardRadio) {
                standardRadio.disabled = false;
                standardRadio.checked = true;   // ✅ auto-select Standard
            }

            if(expressRadio)
                expressRadio.disabled = false;

            if(freeRadio)
                freeRadio.checked = false;
            if(freeLabel)
                freeLabel.classList.remove("applied");

            // ✅ Decide whether to keep Express or force Standard
            let selectedShipping = "standard"; // fallback
            if(expressRadio && expressRadio.checked) {
                selectedShipping = "express";
            }
            else if(standardRadio) {
                standardRadio.checked = true;
            }

            // force backend to recalc with standard shipping
            const userId = document.querySelector(".cart-item")?.getAttribute("data-userId");
            fetch(`/user/cartSummaryAjax?uid=${userId}&shipping=standard`)
                .then(res => res.json())
                .then(stdData => {
                if(stdData.success){
                    document.getElementById("cart-shipping").textContent = "₹ " + stdData.shipping;
                    document.getElementById("cart-total").textContent = "₹ " + stdData.totalOrderPrice;
                }
            });
        }
}



function clearCart(userId) {
console.log("Clear");
    Swal.fire({
        title: "Are you sure?",
        text: "This will remove all items from your cart.",
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Yes, clear it",
        cancelButtonText: "Cancel"
    })
    .then((result) => {
        if (result.isConfirmed) {
            fetch(`/user/cart/clear?uid=${userId}`, { method: "POST" })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        Swal.fire("Cleared!", "Your cart is now empty.", "success");
                        location.reload();
                    }
                })
                .catch(err => {
                    console.error("Clear cart failed:", err);
                    Swal.fire("Error!", "Could not clear your cart. Try again.", "error");
                });
        }
    });
}









