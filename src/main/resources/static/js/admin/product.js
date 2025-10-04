function updateSubcategories(selectElement) {
    const categoryId = selectElement.value; // now value must be ID instead of name
    const subcategorySelect = document.getElementById("subcategorySelect");
    const sizeContainer = document.getElementById("sizeContainer");
    const singleStockContainer = document.getElementById("singleStockContainer");


    // Clear previous subcategories
    subcategorySelect.innerHTML = '<option value="" disabled selected>Select sub-category</option>';

    // Hide size container by default
    sizeContainer.style.display = "none";

    if(categoryId){
       fetch(`/admin/subcategories/${categoryId}`)
           .then(response => response.json())
           .then(data => {
               data.forEach(sub => {
                   let option = document.createElement("option");
                   option.value = sub.id;
                   option.text = sub.subcategoryName;
                   subcategorySelect.appendChild(option);
               });
           })
           .catch(err => console.error("Error fetching subcategories:", err));

       // Show size dropdown if category is Clothing
       const selectedCategoryText = selectElement.options[selectElement.selectedIndex].text;
       if(selectedCategoryText.toLowerCase() === "clothing"){
            sizeContainer.style.display = "block";
            singleStockContainer.style.display = "none";
       }
    }
}


function submitProductForm() {
    const form = document.getElementById("productForm");

    if (!form.checkValidity()) {
        form.querySelectorAll(":invalid").forEach(el => el.classList.add("is-invalid"));
        Swal.fire({ icon: 'error', title: 'Missing Required Fields', text: 'Please fill all required fields.' });
        return;
    }

    form.querySelectorAll(".is-invalid").forEach(el => el.classList.remove("is-invalid"));

    const formData = new FormData(form);

    // Colors
    formData.delete('colorNames');
    formData.delete('hexCodes');
    document.querySelectorAll('#colorsWrapper .form-check-input[type="checkbox"]:checked').forEach(cb => {
        const hexInput = cb.nextElementSibling;
        formData.append('colorNames', cb.value);
        formData.append('hexCodes', hexInput.value);
    });

    // Sizes
    const sizeContainer = document.getElementById("sizeContainer");
    if(sizeContainer && sizeContainer.style.display !== "none") {
        formData.delete('sizeNames');
        formData.delete('sizeStocks');

        document.querySelectorAll('#sizesWrapper .mb-2').forEach(div => {
            const checkbox = div.querySelector('.sizeCheckbox');
            const stockInput = div.querySelector('.sizeStockInput');
            if(checkbox.checked && stockInput.value) {
                formData.append('sizeNames', checkbox.value);
                formData.append('sizeStocks', parseInt(stockInput.value));
            }
        });
    }
    else {
        const stockInput = document.querySelector('input[name="stock"]');
        if (stockInput) formData.set('stock', parseInt(stockInput.value));
    }

    // Debug
    for (let pair of formData.entries()) console.log(pair[0], pair[1]);

    fetch('/admin/saveProduct', { method: 'POST', body: formData })
        .then(res => res.json())
        .then(data => {
            if (data.status === 'success') {
                Swal.fire({ icon: 'success', title: 'Product Added!', text: data.message, timer: 2000, showConfirmButton: false });
                setTimeout(() => { form.reset(); window.location.href = '/admin/products'; }, 3000);
            } else {
                Swal.fire({ icon: 'error', title: 'Error!', text: data.message });
            }
        })
        .catch(err => {
            console.error(err);
            Swal.fire({ icon: 'error', title: 'Oops!', text: 'Something went wrong. Try again.' });
        });
}





function updateEditProductForm(productId) {
    const form = document.querySelector(`#editProductModal__${productId} form`);
    const formData = new FormData(form);

    console.log(formData.get('id'));

    // --- Handle Colors ---
    formData.delete('colorNames');
        formData.delete('hexCodes');
        document.querySelectorAll('#colorsWrapper .form-check-input[type="checkbox"]:checked').forEach(cb => {
            const hexInput = cb.nextElementSibling;
            formData.append('colorNames', cb.value);
            formData.append('hexCodes', hexInput.value);
        });

       console.log(formData.entries());


    const categoryEl = form.querySelector('input[name="category"]');
    const category = categoryEl ? categoryEl.value : '';
    console.log(category);
    if(category === 'Clothing') {
        formData.delete('sizeNames');
        formData.delete('sizeStocks');

        const sizeCheckboxes = form.querySelectorAll('.sizeCheckbox');
        const sizeInputs = form.querySelectorAll('.sizeStockInput');

        sizeCheckboxes.forEach((cb, idx) => {
            if(cb.checked) {
                formData.append('sizeNames', cb.value);
                formData.append('sizeStocks', sizeInputs[idx].value);
            }
        });
    }

    // Validate required fields
    if(!form.checkValidity()) {
        form.querySelectorAll(":invalid").forEach(el => el.classList.add("is-invalid"));
        Swal.fire({
            icon: 'error',
            title: 'Missing Required Fields',
            text: 'Please fill all required fields.'
        });
        return;
    }
    form.querySelectorAll(".is-invalid").forEach(el => el.classList.remove("is-invalid"));


    fetch('/admin/updateProduct', {
        method: 'POST',
        body: formData
    })
    .then(resp => resp.json())
    .then(data => {
        if(data.status === 'success'){
            // Close modal
            const modalEl = document.getElementById(`editProductModal__${productId}`);
            const modal = bootstrap.Modal.getInstance(modalEl);
            modal.hide();
            Swal.fire({
                icon: 'success',
                title: 'Product Updated!',
                text: data.message,
                timer: 2000,
                showConfirmButton: false
            }).then(() => {
//                window.location.href = '/admin/products'; // go back to category list
            });
        }
        else {
            Swal.fire({
                icon: 'error',
                title: 'Error!',
                text: data.message
            });
        }
    })
    .catch(err => {
        console.log(err);
        Swal.fire({
            icon: 'error',
            title: 'Oops!',
            text: 'Something went wrong.'
        });
    });
}


function deleteProduct(productId) {
    Swal.fire({
        title: 'Are you sure?',
        text: "You won't be able to revert this!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
        if(result.isConfirmed) {
            fetch('/admin/deleteProduct/' + productId, { method: 'GET' })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'success') {
                    // Remove the product row dynamically
                    document.getElementById('productRow_' + productId).remove();

                    Swal.fire(
                        'Deleted!',
                        'Product has been deleted.',
                        'success'
                    );
                } else {
                    Swal.fire(
                        'Error!',
                        data.message,
                        'error'
                    );
                }
            })
            .catch(error => {
                console.error(error);
                Swal.fire(
                    'Error!',
                    'Something went wrong. Try again.',
                    'error'
                );
            });
        }
    });
}
