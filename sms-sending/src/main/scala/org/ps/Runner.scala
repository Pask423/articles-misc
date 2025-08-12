package org.ps

import org.ps.outcome.{SmsSendCorrectly, SmsSendOutcome, SmsSendingFailed}
import org.ps.sender.Vonage

object Runner extends App {

  private val sendCorrectly: SmsSendOutcome =
    Vonage.send("Receiver phone number goes here", "Hello SMS world", "Scala SMS")

  sendCorrectly match {
    case SmsSendCorrectly =>
      println("Message has been send correctly")
    case SmsSendingFailed =>
      println("Failed to send message")
  }

}
