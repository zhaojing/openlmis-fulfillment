package org.openlmis.fulfillment.web.errorhandler;

import org.openlmis.fulfillment.referencedata.service.ReferenceDataRetrievalException;
import org.openlmis.fulfillment.service.OrderFileException;
import org.openlmis.fulfillment.service.OrderSenderException;
import org.openlmis.fulfillment.service.OrderStorageException;
import org.openlmis.fulfillment.service.ReportingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller advice responsible for handling errors specific to fulfilment.
 */
@ControllerAdvice
public class GeneralErrorHandling extends AbstractErrorHandling {

  @ExceptionHandler(OrderFileException.class)
  public ErrorResponse handleOrderFileGenerationError(OrderFileException ex) {
    return logErrorAndRespond("Unable to generate the order file", ex);
  }

  @ExceptionHandler(ReportingException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handlerReportingException(ReportingException ex) {
    return logErrorAndRespond("Reporting error", ex);
  }

  /**
   * Handles the {@link DataIntegrityViolationException} which signals a violation of some sort
   * of a db constraint like unique. Returns error 409 (CONFLICT) and a JSON representation of the
   * error as the body.
   * @param ex the exception that caused the issue
   * @return the error response
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  @ResponseBody
  public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    return logErrorAndRespond("Data integrity violation", ex);
  }

  @ExceptionHandler(ReferenceDataRetrievalException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorResponse handleRefDataException(ReferenceDataRetrievalException ex) {
    return logErrorAndRespond("Error fetching from reference data", ex);
  }

  @ExceptionHandler(OrderStorageException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorResponse handleOrderStorageException(OrderStorageException ex) {
    return logErrorAndRespond("Unable to storage the order", ex);
  }

  @ExceptionHandler(OrderSenderException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorResponse handleOrderSenderException(OrderSenderException ex) {
    return logErrorAndRespond("Unable to send the order", ex);
  }

}
