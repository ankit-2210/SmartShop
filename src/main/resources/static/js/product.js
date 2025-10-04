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
                }
                else {
                    Swal.fire("Info", data.message || "Item not found.", "info");
                }
            })
            .catch(() => Swal.fire("Error", "Failed to remove item", "error"));
        }
    });
}
