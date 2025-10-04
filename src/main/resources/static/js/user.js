function saveUser(event) {
    event.preventDefault(); // stop form reload

    let form = document.getElementById("registerForm");
    console.log(form);
    let formData = new FormData(form);

    fetch("/saveUser", {
        method: "POST",
        body: formData
    })
    .then(res => res.json())
    .then(data => {
        if (data.status === "success") {
            Swal.fire({
                title: "Success!",
                text: data.message,
                icon: "success",
                timer: 3000,
                showConfirmButton: false
            }).then(() => {
                setTimeout(() => {
                    window.location.href = "/signin";
                }, 5000);
            });
        }
        else {
            Swal.fire({
                title: "Error!",
                text: data.message,
                icon: "error",
                confirmButtonText: "OK"
            });
        }
    })
    .catch(err => {
        Swal.fire("Error!", "Something went wrong.", "error");
    });

    return false; // prevent normal submit
}



function updateProfileAjax(button) {
    Swal.fire({
        title: 'Are you sure?',
        text: "Do you want to update your profile?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, update it!'
    }).then((result) => {
        if (result.isConfirmed) {
            const form = document.getElementById('profileForm');
            const formData = new FormData(form);

            $.ajax({
                url: '/user/updateProfile',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(response) {
                    Swal.fire(
                        'Updated!',
                        'Your profile has been updated successfully!',
                        'success'
                    ).then(() => {
                        location.reload(); // refresh to show updated data
                    });
                },
                error: function(err) {
                    Swal.fire(
                        'Error!',
                        'Something went wrong. Please try again!',
                        'error'
                    );
                }
            });
        }
    });
}


function updatePassword(){
     const currPassword = document.getElementById('currPassword').value;
     const newPassword = document.getElementById('newPassword').value;
     const confirmPassword = document.getElementById('confirmPassword').value;

     if(!currPassword || !newPassword || !confirmPassword) {
        Swal.fire({
            icon: 'warning',
            title: 'Oops...',
            text: 'Please fill all fields!'
        });
        return;
     }

     if(newPassword !== confirmPassword) {
        Swal.fire({
            icon: 'error',
            title: 'Oops...',
            text: 'New Password and Confirm Password do not match!'
        });
        return;
     }

     // AJAX call
     fetch("/user/change-password", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': /*[[${_csrf.token}]]*/ ''
        },
        body: new URLSearchParams({
            currPassword: currPassword,
            newPassword: newPassword
        })
     })
     .then(response => response.json())
     .then(data => {
        if(data.status === 'success') {
            Swal.fire({
                icon: 'success',
                title: 'Success!',
                text: data.message
            })
            .then(() => {
                // Optional: refresh page or reset form
                document.getElementById('changePasswordForm').reset();
             });
        }
        else{
            Swal.fire({
                icon: 'error',
                title: 'Error!',
                text: data.message
            });
        }
     })
     .catch(error => {
        console.error(error);
        Swal.fire({
            icon: 'error',
            title: 'Error!',
            text: 'Something went wrong!'
        });
     });

}

function submitReviews(orderId) {
    console.log(orderId);
    const reviews=[];

    document.querySelectorAll(`#reviewModal${orderId} .review-item`).forEach(item => {
        reviews.push({
            productId: item.querySelector("input[name='productId']").value,
            orderId: orderId,
            rating: item.querySelector("select[name='rating']").value,
            comment: item.querySelector("textarea[name='comment']").value
        });
    });

    console.log(reviews);

    fetch("/user/addReviews", {
        method: "POST",
        headers:{
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ reviews })
    })
    .then(res=>res.json())
    .then(data=>{
         if(data.status === "success") {
            Swal.fire({
                title: "Success!",
                text: data.message,
                icon: "success",
                confirmButtonText: "OK"
            }).then(() => {
                const modal = bootstrap.Modal.getInstance(document.getElementById(`reviewModal${orderId}`));
                modal.hide();
                setTimeout(() => {
                    window.location.href = "/user/myreviews";
                }, 3000);
            });
        }
        else {
            Swal.fire({
                title: "Error!",
                text: data.message,
                icon: "error",
                confirmButtonText: "OK"
            });
        }
    })
    .catch(err => {
        Swal.fire("Error", "Could not submit reviews.", "error");
    });

}


function updateReview(reviewId){
    const rating = document.getElementById("rating" + reviewId).value;
    const comment = document.getElementById("comment" + reviewId).value;

    console.log(rating, comment);

    fetch("/user/updateReview",{
        method: "POST",
        headers:{
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            reviewId: reviewId,
            rating: rating,
            comment: comment
        })
    })
    .then(response => response.json())
    .then(data => {
    if(data.status === "success") {
        Swal.fire({
            title: "Updated!",
            text: data.message,
            icon: "success",
            timer: 2000,
            showConfirmButton: false
        })
        .then(() => {
            // Close modal
            const modal = bootstrap.Modal.getInstance(document.getElementById(`editReviewModal${reviewId}`));
            modal.hide();

            // Refresh the page OR dynamically update
            location.reload();
        });
    }
    else{
         Swal.fire("Error!", data.message, "error");
    }
    })
    .catch(err => {
        Swal.fire("Error!", "Something went wrong", "error");
        console.error(err);
     });
}


function saveAddress(){
    let form = document.getElementById("addressForm");
    const data = Object.fromEntries(new FormData(form).entries());

    fetch("/user/address/add", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
         },
         body: JSON.stringify(data)
    })
    .then(res => {
        if(res.ok){
            Swal.fire({
                icon: 'success',
                title: 'Address Saved!',
                text: 'Your new address has been added successfully.',
                confirmButtonColor: '#3085d6'
            }).then(() => {
                location.reload(); // reload page to show updated addresses
            });
        }
        else{
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Something went wrong while saving the address.'
            });
        }
    }).catch(() => {
        Swal.fire({
            icon: 'error',
            title: 'Network Error',
            text: 'Could not connect to the server.'
        });
    });
}


function removeAddress(addressId){
    Swal.fire({
        title: 'Are you sure?',
        text: "This address will be permanently deleted!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, delete it!'
    })
    .then((result)=>{
         if(result.isConfirmed){
            fetch(`/user/address/${addressId}`, {
                method: 'DELETE',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
            .then(response => response.text())
            .then(data => {
                Swal.fire('Deleted!', data, 'success');
                // remove card from DOM
                document.getElementById("address-card-" + addressId).remove();
            })
            .catch(error => {
                Swal.fire('Error!', 'Something went wrong.', 'error');
            });
         }
    })

}


function makeDefault(addressId){
    Swal.fire({
        title: 'Set as Default?',
        text: "This address will become your default address.",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, set it!'
    })
    .then((result)=>{
        fetch(`/user/address/${addressId}/default`, {
            method: "PUT",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
        .then(res => {
            if(res.ok)
                return res.text();
            throw new Error("Failed to update default address");
        })
        .then(message => {
            Swal.fire({
                icon: "success",
                title: "Updated!",
                text: message
            })
            .then(() => location.reload());
        })
        .catch(err => Swal.fire("Error", err.message, "error"));

    })
}


function updateAddress(addressId) {
    let form = document.getElementById("editAddressForm-" + addressId);
    let data = Object.fromEntries(new FormData(form).entries());
    data.isDefault = form.querySelector("[name='isDefault']").checked;

    fetch(`/user/address/${addressId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
    .then(res => {
        if(res.ok) {
            Swal.fire({
                icon: 'success',
                title: 'Address Updated!',
                text: 'Your address has been successfully updated.',
                confirmButtonColor: '#3085d6'
            }).then(() => {
                // Close the modal
                bootstrap.Modal.getInstance(document.getElementById("editAddressModal-" + addressId)).hide();
                location.reload();
            });
        }
        else {
            Swal.fire({
                icon: 'error',
                title: 'Update Failed',
                text: 'Something went wrong while updating the address.'
            });
        }
    })
    .catch(() => {
        Swal.fire({
            icon: 'error',
            title: 'Network Error',
            text: 'Could not connect to the server.'
        });
    });
}



function saveNewCard(){
    let form = document.getElementById("addCardForm");
    let data = Object.fromEntries(new FormData(form).entries());
    data.isDefault = form.querySelector("[name='isDefault']").checked;

    fetch("/user/payment/add", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
    .then(res => {
        if(res.ok){
            Swal.fire({
                icon: 'success',
                title: 'Payment Details Saved!',
                text: 'Your new payment details has been added successfully.',
                confirmButtonColor: '#3085d6'
            }).
            then(() => {
                location.reload(); // reload page to show updated addresses
            });
        }
        else{
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Something went wrong while saving the details.'
            });
        }
    })
    .catch(() => {
        Swal.fire({
            icon: 'error',
            title: 'Network Error',
            text: 'Could not connect to the server.'
        });
    });

}


function updateCard(cardId) {
    let form = document.getElementById("editPaymentCardForm-" + cardId);
    let data = Object.fromEntries(new FormData(form).entries());
    data.isDefault = form.querySelector("[name='isDefault']").checked;

    fetch(`/user/paymentCard/${cardId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
    .then(res => {
        if(res.ok) {
            Swal.fire({
                icon: 'success',
                title: 'Payment Details Updated!',
                text: 'Your details has been successfully updated.',
                confirmButtonColor: '#3085d6'
            }).then(() => {
                // Close the modal
                bootstrap.Modal.getInstance(document.getElementById("editPaymentCardModal-" + cardId)).hide();
                location.reload();
            });
        }
        else {
            Swal.fire({
                icon: 'error',
                title: 'Update Failed',
                text: 'Something went wrong while updating the address.'
            });
        }
    })
    .catch(() => {
        Swal.fire({
            icon: 'error',
            title: 'Network Error',
            text: 'Could not connect to the server.'
        });
    });
}



function removeCard(cardId){
    Swal.fire({
        title: 'Are you sure?',
        text: "This card will be permanently deleted!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, delete it!'
    })
    .then((result)=>{
         if(result.isConfirmed){
            fetch(`/user/paymentCard/${cardId}`, {
                method: 'DELETE',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
            .then(response => response.text())
            .then(data => {
                Swal.fire('Deleted!', data, 'success');
                // remove card from DOM
                document.getElementById("payment-card-" + cardId).remove();
            })
            .catch(error => {
                Swal.fire('Error!', 'Something went wrong.', 'error');
            });
         }
    })

}

function makeDefaultCard(cardId){
    Swal.fire({
        title: 'Set as Default?',
        text: "This card details will become your default.",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, set it!'
    })
    .then((result)=>{
        fetch(`/user/paymentCard/${cardId}/default`, {
            method: "PUT",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
        .then(res => {
            if(res.ok)
                return res.text();
            throw new Error("Failed to update default card");
        })
        .then(message => {
            Swal.fire({
                icon: "success",
                title: "Updated!",
                text: message
            })
            .then(() => location.reload());
        })
        .catch(err => Swal.fire("Error", err.message, "error"));

    })
}


let deleteAttempts=0;
function deleteAccount(userEmail){
    const deleteBtn = document.getElementById('deleteAccountBtn');

    Swal.fire({
        title: 'Are you sure?',
        text: "Once you delete your account, this cannot be undone!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Yes, delete it!',
        cancelButtonText: 'Cancel'
    })
    .then((result) => {
        if (result.isConfirmed) {
            askEmail();
        }
    });

    function askEmail() {
        Swal.fire({
            title: 'Please enter your email to confirm',
            input: 'email',
            inputLabel: 'Your email',
            inputPlaceholder: 'Enter your email',
            showCancelButton: true,
            confirmButtonText: 'Delete Account',
            cancelButtonText: 'Cancel',
            preConfirm: (email) => {
                const loggedInEmail = userEmail;

                // validate email match
                if(email !== loggedInEmail) {
                    deleteAttempts++;
                    if(deleteAttempts >= 3){
                        deleteBtn.disabled = true;
                        Swal.fire('Too many wrong attempts!', 'Please wait 30 seconds before trying again.', 'error');
                        setTimeout(() => {
                            deleteBtn.disabled = false;
                            deleteAttempts = 0; // reset after timeout
                        }, 30000);
                        return false;
                    }
                    Swal.showValidationMessage(`Email does not match! Attempts left: ${3 - deleteAttempts}`);
                    return false;
                }

                return email;
            }
        }).then((emailResult) => {
            if(emailResult.isConfirmed){
                // Reset attempts
                deleteAttempts = 0;
                fetch('/user/delete-account', {
                    method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                    body: JSON.stringify({ email: emailResult.value })
                })
                .then(response => response.json())
                .then(data => {
                    if(data.status === 'success') {
                        Swal.fire({
                            icon: 'success',
                            title: 'Deleted!',
                            text: 'Your account has been deleted.',
                            allowOutsideClick: false,
                            allowEscapeKey: false
                        }).then(() => {
                            window.location.href = '/signin';
                        });
                    }
                    else {
                        Swal.fire({
                            icon: 'error',
                            title: 'Error!',
                            text: data.message || 'Unable to delete account.'
                        });
                    }
                })
                .catch(err => {
                    console.error(err);
                    Swal.fire({
                        icon: 'error',
                        title: 'Error!',
                        text: 'Something went wrong!'
                    });
                });
            }
        });
    }
}


























