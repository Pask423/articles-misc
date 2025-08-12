package org.ps.outcome

sealed trait SmsSendOutcome

case object SmsSendCorrectly extends SmsSendOutcome

case object SmsSendingFailed extends SmsSendOutcome