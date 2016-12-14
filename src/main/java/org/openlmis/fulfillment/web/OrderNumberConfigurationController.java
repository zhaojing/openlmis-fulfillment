package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.dto.OrderNumberConfigurationDto;
import org.openlmis.fulfillment.repository.OrderNumberConfigurationRepository;
import org.openlmis.fulfillment.web.validator.OrderNumberConfigurationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Iterator;

import javax.validation.Valid;

@Controller
public class OrderNumberConfigurationController extends BaseController {

  @Autowired
  private OrderNumberConfigurationRepository orderNumberConfigurationRepository;

  @Autowired
  private OrderNumberConfigurationValidator validator;

  @InitBinder
  protected void initBinder(final WebDataBinder binder) {
    binder.addValidators(validator);
  }

  /**
   * Saves given OrderNumberConfiguration to database.
   *
   * @param orderNumberConfigurationDto object to save.
   * @return Response entity with Http status code.
   */
  @RequestMapping(value = "/orderNumberConfigurations", method = RequestMethod.POST)
  public ResponseEntity<Object> saveOrderNumberConfigurations(
      @RequestBody @Valid OrderNumberConfigurationDto orderNumberConfigurationDto,
      BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return new ResponseEntity<>(getErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }
    OrderNumberConfiguration orderNumberConfiguration = OrderNumberConfiguration
        .newOrderNumberConfiguration(orderNumberConfigurationDto);

    Iterator<OrderNumberConfiguration> it = orderNumberConfigurationRepository.findAll().iterator();

    if (it.hasNext()) {
      orderNumberConfiguration.setId(it.next().getId());
    }

    OrderNumberConfiguration savedOrderNumberConfiguration =
        orderNumberConfigurationRepository.save(orderNumberConfiguration);

    OrderNumberConfigurationDto orderNumberConfigurationDto1 = new OrderNumberConfigurationDto();
    savedOrderNumberConfiguration.export(orderNumberConfigurationDto1);

    return new ResponseEntity<>(orderNumberConfigurationDto1, HttpStatus.OK);
  }

  /**
   * Get orderNumberConfiguration.
   *
   * @return OrderNumberConfiguration.
   */
  @RequestMapping(value = "/orderNumberConfigurations", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<OrderNumberConfigurationDto> getOrderFileTemplate() {
    Iterator<OrderNumberConfiguration> it = orderNumberConfigurationRepository.findAll().iterator();

    if (!it.hasNext()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    OrderNumberConfigurationDto orderNumberConfigurationDto = new OrderNumberConfigurationDto();
    it.next().export(orderNumberConfigurationDto);

    return new ResponseEntity<>(orderNumberConfigurationDto, HttpStatus.OK);
  }
}
