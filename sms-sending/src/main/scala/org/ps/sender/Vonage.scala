package org.ps.sender

import com.vonage.client.VonageClient
import com.vonage.client.sms.messages.TextMessage
import com.vonage.client.sms.{MessageStatus, SmsClient, SmsSubmissionResponse}
import org.ps.outcome.{SmsSendCorrectly, SmsSendOutcome, SmsSendingFailed}
import org.ps.properties.ApplicationProperties

object Vonage extends SmsSender {

  private val client: SmsClient = VonageClient
    .builder()
    .apiKey(ApplicationProperties.vonageApiKey)
    .apiSecret(ApplicationProperties.vonageApiSecret)
    .build
    .getSmsClient

  override def send(receiverNumber: String, message: String, senderName: String): SmsSendOutcome = {
    val smsMessage: TextMessage = new TextMessage(senderName, receiverNumber, message)

    val response: SmsSubmissionResponse = client.submitMessage(smsMessage)

    val responseStatus = response.getMessages.get(0).getStatus
    if (MessageStatus.OK.equals(responseStatus)) SmsSendCorrectly
    else SmsSendingFailed
  }
}
