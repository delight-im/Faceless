# Twilio

## Reliability of receiving SMS

> At the receiving end of SMS messages, Twilio cannot guarantee that every international SMS message sent to us will be received in your SMS log. Twilio accepts these messages, however it is the responsibility of the sending phone number carrier to deliver the SMS message to our network.
> 
> I'd particularly recommend using our UK mobile numbers or Swedish mobile numbers. These numbers have been pretty successful at receiving SMS from other countries.
> 
> There are two other things you should be aware of which affect the delivery of inbound international SMS: not all users will have plans which support international SMS, and international roaming might make the phone unable to send or receive SMS. If your users are primarily travelers, it's very likely that you will see issues of this type.

*-- Lauren Buchsbaum (Twilio)*
