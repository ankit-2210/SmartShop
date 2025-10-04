function paymentGateway(button) {
    // Get selected payment method
    const selected = document.querySelector('input[name="payment-method"]:checked').id;

    if (selected === "credit-card") {
        // Handle Credit/Debit card payment
        console.log("Processing Credit/Debit Card...");
        // Example: call your card payment API here
        processCardPayment();
    }
    else if (selected === "paypal") {
        // Redirect to PayPal
        console.log("Redirecting to PayPal...");
        processPaypalPayment();
    }
    else if (selected === "razorpay") {
        // Call Razorpay Checkout
        console.log("Launching Razorpay...");
        processRazorpayPayment();
    }
}

function processCardPayment() {

}

function processPaypalPayment() {


}

function processRazorpayPayment() {
    console.log("Payment Started..");

    let amount = $("#payment_field").text();   // UI value, e.g. "₹186.99"
        let numericAmount = amount.replace(/[^\d.]/g, ""); // "186.99"

        // Convert to number and round to 2 decimal places
        let roundedAmount = parseFloat(numericAmount).toFixed(2);
        let finalAmount = parseFloat(roundedAmount);
        console.log("Final Amount: ", roundedAmount);

        if (!roundedAmount || isNaN(roundedAmount)) {
            swal("Failed!", "Amount is required !!", "error");
            return;
        }

        $.ajax({
            url: "/user/create_order",
            data: JSON.stringify({
                    amount: finalAmount,                  // ✅ number not string
                    firstName: $("#firstName").val(),
                    lastName: $("#lastName").val(),
                    email: $("#email").val(),
                    mobileNo: $("#mobileNo").val(),
                    address: $("#address").val(),
                    city: $("#city").val(),
                    state: $("#state").val(),
                    pincode: $("#pincode").val(),
                    country: $("#country").val(),
                    paymentType: "razorpay"
                }),
            contentType: "application/json",
            type: "POST",
            dataType: "json",
            success: function(response) {
                if (response.status === 'created') {
                    let options = {
                        key: 'rzp_test_YfFqvXPL8JPBdJ',
                        amount: response.amount,  // already in paise
                        currency: 'INR',
                        name: 'Shopping Site',
                        description: 'Payment',
                        order_id: response.id,
                        handler: function(res) {
                            console.log(res.razorpay_payment_id);
                            console.log(res.razorpay_order_id);
                            console.log(res.razorpay_signature);

                            let paymentType = "razorpay";
                            updatePaymentOnServer(
                                res.razorpay_payment_id,
                                res.razorpay_order_id,
                                "Paid",
                                paymentType,

                                function(orderId){
                                    // ✅ Redirect to success page with orderId
                                    console.log(orderId);
                                    swal("Good Job!", "Congrats!! Payment Successfull !!", "success");
                                    window.location.href = "/user/success?orderId=" + orderId;
                                }
                            );


                        },
                        prefill: {
                            name: "",
                            email: "",
                            contact: ""
                        },
                        notes: {
                            address: "Learn Spring Boot"
                        },
                        theme: {
                            color: "#3399cc"
                        }
                    };

                    let rzp = new Razorpay(options);
                    rzp.on('payment.failed', function(res) {
                        console.log(res.error);
                        swal("Failed!", "Oops !! Payment failed", "error");
                    });

                    rzp.open();
                }

                console.log(response);
            },
            error: function(error) {
                alert("Something went wrong !!");
            }
        });
    }

const updatePaymentOnServer = (payment_id, order_id, status, payment_type, callback) => {

	$.ajax({
		url: "/user/update_order",
        data: JSON.stringify({
            payment_id: payment_id,
            order_id: order_id,
            status: status,
            payment_type: payment_type
        }),
        contentType: "application/json",
		type: "POST",
		dataType: "json",
		success: function(response){
			console.log("Server update response:", response);

            if(response.success && callback){
                callback(response.orderId); // ✅ Always provided now
            }
            else{
                swal("Failed!", response.message, "error");
            }
		},
		error: function(error){
			swap("Failed!", "Your payment is Successful, but we did not get on server, we will contact you as soon as possible !!", "error");
		}

	});
}
