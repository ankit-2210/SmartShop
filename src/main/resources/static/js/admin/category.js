function addCategory(){
    var form = document.getElementById('categoryForm');
    var formData = new FormData(form);

     // Trim values
    formData.set('categoryName', formData.get('categoryName').trim());
    formData.set('subcategoryName', formData.get('subcategoryName').trim());

    // Client-side validation
    if(!formData.get('categoryName')){
        Swal.fire('Oops!', 'Category name is required!', 'warning');
        return;
     }

    // Client-side validation: ensure a file is selected
    var fileInput = form.querySelector('input[name="file"]');
    if(!fileInput || fileInput.files.length === 0) {
        Swal.fire({
            icon: 'warning',
            title: 'Oops!',
            text: 'Please upload a category image!',
            confirmButtonText: 'OK'
        });
        return; // stop submission
    }


    var submitButton = form.querySelector('button[type="button"]');
    submitButton.disabled = true;

    Swal.fire({
        title: 'Saving...',
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
    });

    $.ajax({
        type: 'POST',
        url: '/admin/saveCategory',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            Swal.close();
            submitButton.disabled = false;

            if(response.status === 'success') {
                Swal.fire('Success!', response.message, 'success').then(() => {
                    form.reset();
                    window.location.reload();
                });
            }
            else {
                Swal.fire('Oops...', response.message, 'error');
            }
        },
        error: function() {
            Swal.close();
            submitButton.disabled = false;
            Swal.fire('Oops...', 'Something went wrong! Please try again.', 'error');
        }
    });
}


function updateCategory(id) {
    let form = document.querySelector(`#editCategoryModal__${id} form`);
    let formData = new FormData(form);

    fetch("/admin/updateCategory", {
        method: "POST",
        body: formData
    })
    .then(response => {
        if(!response.ok)
            throw new Error("Failed to update");
        return response.text();
    })
    .then(() => {
        Swal.fire({
            icon: "success",
            title: "Updated!",
            text: "Category updated successfully",
            timer: 3000,
            showConfirmButton: false
        }).then(() => {
            let modalEl = document.getElementById(`editCategoryModal__${id}`);
            let modal = bootstrap.Modal.getInstance(modalEl);
            modal.hide();

            // ✅ Reload page (after closing modal)
            setTimeout(() => location.reload(), 3000);
        });
    })
    .catch(error => {
        console.error(error);
        Swal.fire({
            icon: "error",
            title: "Oops...",
            text: "Something went wrong while updating!"
        });
    });
}



function deleteSubCategory(subcategoryId) {
    console.log(subcategoryId);
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
            $.ajax({
                type: 'DELETE', // or 'POST' if you prefer
                url: '/admin/deleteSubCategory/' + subcategoryId,
                success: function(response) {
                    if(response.status === 'success'){
                        Swal.fire('Deleted!', response.message, 'success')
                            .then(() => window.location.href = '/admin/category');
                    }
                    else if(response.status === 'warning'){
                        Swal.fire('Warning!', response.message, 'warning');
                    }
                    else{
                        Swal.fire('Error!', response.message, 'error');
                    }
                },
                error: function(xhr, status, error) {
                    Swal.fire(
                        'Error!',
                        'Something went wrong! Please try again.',
                        'error'
                    );
                }
            });
        }
    })
}



function addBrand() {
    var form = document.getElementById('brandForm'); // make sure your brand form has this id
    var formData = new FormData(form);

    // Trim values
    formData.set('name', formData.get('name').trim());

    // Client-side validation
    if (!formData.get('name')) {
        Swal.fire('Oops!', 'Brand name is required!', 'warning');
        return;
    }

    if (!formData.get('categoryId')) {
        Swal.fire('Oops!', 'Please select a category!', 'warning');
        return;
    }

    if (!formData.get('subcategoryId')) {
        Swal.fire('Oops!', 'Please select a sub-category!', 'warning');
        return;
    }

    // Validate file
    var fileInput = form.querySelector('input[name="file"]');
    if (!fileInput || fileInput.files.length === 0) {
        Swal.fire({
            icon: 'warning',
            title: 'Oops!',
            text: 'Please upload a brand logo!',
            confirmButtonText: 'OK'
        });
        return;
    }

    var submitButton = form.querySelector('button[type="button"]');
    submitButton.disabled = true;

    Swal.fire({
        title: 'Saving...',
        text: 'Please wait while we save your brand.',
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
    });

    $.ajax({
        type: 'POST',
        url: '/admin/brands/add', // your controller endpoint
        data: formData,
        processData: false,
        contentType: false,
        dataType: 'json',
        success: function(response) {
            Swal.close();
            submitButton.disabled = false;

            if(response.status === 'success') {
                Swal.fire({
                    icon: 'success',
                    title: 'Success!',
                    text: response.message,
                    confirmButtonText: 'OK'
                }).then(() => {
                    form.reset();
                    window.location.reload();
                });
            }
            else {
                Swal.fire({
                    icon: 'error',
                    title: 'Error!',
                    text: response.message,
                    confirmButtonText: 'Try Again'
                });
            }
        },
        error: function() {
            Swal.close();
            submitButton.disabled = false;
            Swal.fire({
                icon: 'error',
                title: 'Error!',
                text: 'Something went wrong! Please try again.',
                confirmButtonText: 'OK'
            });
        }
    });
}


function updateBrand(id) {
    const form = document.querySelector(`#editBrandModal__${id} form`);
    const formData = new FormData(form);

    Swal.fire({
        title: 'Updating...',
        text: 'Please wait while we update the brand.',
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
    });

    fetch('/admin/brands/update', {
        method: 'POST',
        body: formData
    })
    .then(res => res.json())
    .then(data => {
        Swal.close();
        if (data.status === 'success') {
            Swal.fire({
                icon: 'success',
                title: 'Updated!',
                text: data.message,
                timer: 2000,
                showConfirmButton: false
            })
            .then(() => {
                const modalEl = document.getElementById(`editBrandModal__${id}`);
                const modal = bootstrap.Modal.getInstance(modalEl);
                modal.hide();
                location.reload();
            });
        }
        else {
            Swal.fire('Error', data.message, 'error');
        }
    })
    .catch(err => {
        Swal.close();
        Swal.fire('Error', 'Something went wrong while updating.', 'error');
    });
}


function deleteBrand(id){
     Swal.fire({
        title: 'Are you sure?',
        text: "This action will permanently delete the brand.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Yes, delete it!'
     }).then((result)=>{
        if(result.isConfirmed){
            fetch(`/admin/brands/delete/${id}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data =>{
                if(data.status === 'success'){
                    Swal.fire({
                        icon: 'success',
                        title: 'Deleted!',
                        text: data.message,
                        timer: 2000,
                        showConfirmButton: false
                    })
                    .then(()=>location.reload());
                }
                else{
                    Swal.fire('Error', data.message, 'error');
                }
            })
            .catch(err => {
                console.error(err);
                Swal.fire('Error', 'Something went wrong while deleting.', 'error');
            });
        }
     });
}












