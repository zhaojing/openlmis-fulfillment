package org.openlmis.fulfillment.web;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.referencedata.model.FacilityDto;
import org.openlmis.fulfillment.referencedata.model.OrderableProductDto;
import org.openlmis.fulfillment.referencedata.model.ProcessingPeriodDto;
import org.openlmis.fulfillment.referencedata.model.ProcessingScheduleDto;
import org.openlmis.fulfillment.referencedata.model.ProgramDto;
import org.openlmis.fulfillment.referencedata.model.SupervisoryNodeDto;
import org.openlmis.fulfillment.repository.OrderLineItemRepository;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class OrderLineItemControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orderLineItems";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final UUID ID = UUID.fromString("1752b457-0a4b-4de0-bf94-5a6a8002427e");

  @MockBean
  private OrderLineItemRepository orderLineItemRepository;

  @MockBean
  private OrderRepository orderRepository;

  private OrderLineItem orderLineItem = new OrderLineItem();
  private Order order = new Order();

  @Before
  public void setUp() {
    this.setUpBootstrapData();

    OrderableProductDto product = new OrderableProductDto();
    product.setId(UUID.randomUUID());

    FacilityDto facility = new FacilityDto();
    facility.setId(UUID.randomUUID());
    facility.setCode("facilityCode");
    facility.setName("facilityName");
    facility.setDescription("facilityDescription");
    facility.setActive(true);
    facility.setEnabled(true);

    SupervisoryNodeDto supervisoryNode = new SupervisoryNodeDto();
    supervisoryNode.setId(UUID.randomUUID());
    supervisoryNode.setCode("NodeCode");
    supervisoryNode.setName("NodeName");
    supervisoryNode.setFacility(facility);

    ProgramDto program = new ProgramDto();
    program.setId(UUID.randomUUID());
    program.setCode("programCode");

    ProcessingScheduleDto schedule = new ProcessingScheduleDto();
    schedule.setCode("scheduleCode");
    schedule.setName("scheduleName");

    ProcessingPeriodDto period = new ProcessingPeriodDto();
    period.setId(UUID.randomUUID());
    period.setProcessingSchedule(new ProcessingScheduleDto());
    period.setName("periodName");
    period.setStartDate(LocalDate.of(2015, Month.JANUARY, 1));
    period.setEndDate(LocalDate.of(2015, Month.DECEMBER, 31));

    orderLineItem.setOrder(order);
    orderLineItem.setId(UUID.randomUUID());
    orderLineItem.setOrderableProductId(product.getId());
    orderLineItem.setOrderedQuantity(100L);
    orderLineItem.setFilledQuantity(100L);
    orderLineItem.setApprovedQuantity(0L);

    order.setId(UUID.randomUUID());
    order.setExternalId(UUID.randomUUID());
    order.setEmergency(false);
    order.setFacilityId(facility.getId());
    order.setProcessingPeriodId(period.getId());
    order.setOrderCode("O");
    order.setQuotedCost(new BigDecimal("10.00"));
    order.setStatus(OrderStatus.ORDERED);
    order.setProgramId(program.getId());
    order.setCreatedById(UUID.randomUUID());
    order.setRequestingFacilityId(facility.getId());
    order.setReceivingFacilityId(facility.getId());
    order.setSupplyingFacilityId(facility.getId());
    order.setOrderLineItems(new ArrayList<>());
    order.getOrderLineItems().add(orderLineItem);

    given(orderLineItemRepository.findOne(orderLineItem.getId())).willReturn(orderLineItem);
    given(orderLineItemRepository.exists(orderLineItem.getId())).willReturn(true);

    given(orderLineItemRepository.save(any(OrderLineItem.class)))
        .willAnswer(new SaveAnswer<OrderLineItem>());

    given(orderRepository.findOne(order.getId())).willReturn(order);
    given(orderRepository.exists(order.getId())).willReturn(true);
  }

  @Test
  public void shouldCreateOrder() {
    given(orderLineItemRepository.findOne(orderLineItem.getId())).willReturn(null);
    given(orderLineItemRepository.exists(orderLineItem.getId())).willReturn(false);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderLineItem)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllOrders() {
    given(orderLineItemRepository.findAll()).willReturn(Lists.newArrayList(orderLineItem));

    OrderLineItem[] response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderLineItem[].class);

    Iterable<OrderLineItem> orderLineItems = Arrays.asList(response);
    assertTrue(orderLineItems.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateOrderLineItem() {

    orderLineItem.setOrderedQuantity(100L);

    OrderLineItem response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderLineItem.getId())
        .body(orderLineItem)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderLineItem.class);

    assertTrue(response.getOrderedQuantity().equals(100L));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewOrderLineItemIfDoesNotExist() {
    given(orderLineItemRepository.findOne(orderLineItem.getId())).willReturn(null);
    given(orderLineItemRepository.exists(orderLineItem.getId())).willReturn(false);

    orderLineItem.setOrderedQuantity(100L);

    OrderLineItem response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", ID)
        .body(orderLineItem)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderLineItem.class);

    assertTrue(response.getOrderedQuantity().equals(100L));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenOrderLineItem() {

    OrderLineItem response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderLineItem.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderLineItem.class);

    assertTrue(orderLineItemRepository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentOrderLineItem() {
    given(orderLineItemRepository.findOne(orderLineItem.getId())).willReturn(null);
    given(orderLineItemRepository.exists(orderLineItem.getId())).willReturn(false);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderLineItem.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteOrderLineItem() {
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderLineItem.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentOrderLineItem() {
    given(orderLineItemRepository.findOne(orderLineItem.getId())).willReturn(null);
    given(orderLineItemRepository.exists(orderLineItem.getId())).willReturn(false);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", orderLineItem.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}