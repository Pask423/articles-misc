package org.ps.sender

import org.ps.outcome.SmsSendOutcome

trait SmsSender {

  def send(receiverNumber: String, message: String, senderName: String): SmsSendOutcome
}
