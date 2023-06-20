package com.example.joinery.controller;

import com.example.joinery.entity.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller {
    private final SessionFactory sessionFactory = new Configuration()
            .configure("hibernate.cfg.xml")
            .buildSessionFactory();
    private Session session;
    ObservableList<RetailCustomer> retailCustomers = FXCollections.observableArrayList();
    ObservableList<WholesaleCustomer> wholesaleCustomers = FXCollections.observableArrayList();
    ObservableList<Assembly> assemblyServices = FXCollections.observableArrayList();
    ObservableList<Conservation> conservationServices = FXCollections.observableArrayList();
    ObservableList<WoodMaterial> woodMaterials = FXCollections.observableArrayList();
    ObservableList<WoodLikeMaterial> woodLikeMaterials = FXCollections.observableArrayList();
    ObservableList<Chemical> chemicals = FXCollections.observableArrayList();
    ObservableList<Specialization> specializations = FXCollections.observableArrayList();
    ObservableList<Employee> employees = FXCollections.observableArrayList();
    ObservableList<ServiceOrder> serviceOrders = FXCollections.observableArrayList();
    @FXML
    private GridPane ordersView;
    @FXML
    private GridPane newOrderView;

    @FXML
    private Button viewOrdersButton;
    @FXML
    private Button returnButton;

    private Service newService;
    private ServiceOrder newOrder;
    @FXML
    private Button saveCustomer;
    @FXML
    private Button saveService;
    @FXML
    private Button saveEmployee;
    @FXML
    private TextField selectedCustomerText;
    @FXML
    private TextField selectedEmployeeText;
    @FXML
    private TextField selectedServiceText;
    @FXML
    private ChoiceBox customerTypeChoiceBox;
    @FXML
    private Text serviceTypeText;
    @FXML
    private ChoiceBox serviceChoiceBox;
    @FXML
    private Text assemblyProductNameText;
    @FXML
    private Text assemblySizeText;
    @FXML
    private TextField assemblyProductNameTextField;
    @FXML
    private Slider assemblySizeSlider;
    @FXML
    private Label assemblySizeLabel;
    @FXML
    private Text materialText;
    @FXML
    private ChoiceBox materialChoiceBox;
    @FXML
    private Text conservationLevelOfDamageText;
    @FXML
    private ChoiceBox conservationLevelOfDamageChoiceBox;
    @FXML
    private ChoiceBox selectedAssociates;
    @FXML
    private Button removeAssociate;
    @FXML
    private Text specializationText;
    @FXML
    private ChoiceBox specializationChoiceBox;
    @FXML
    private TableView tableViewCustomer;
    @FXML
    private TableView tableViewElement;
    @FXML
    private TableView tableViewEmployee;
    @FXML
    private ChoiceBox customerChoiceBox;
    @FXML
    private TableView tableViewOrder;

    @FXML
    public void initialize() {
        loadDataCustomer();

        newOrder = new ServiceOrder(serviceOrders.size()+1, ServiceOrder.Status.planned, LocalDate.now());

        setChoiceBoxData();

        customerChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<String>) (observable, oldValue, newValue)
                        -> handleChoiceBoxChangeCustomer(newValue)
        );

        customerTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<String>) (observable, oldValue, newValue)
                        -> handleChoiceBoxChangeCustomerType(newValue)
        );


        serviceChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<String>) (observable, oldValue, newValue)
                        -> handleChoiceBoxChangeService(newValue)
        );

        assemblyProductNameTextField.textProperty().addListener(
                (observable, oldValue, newValue)
                        -> handleTextFieldChangeProductName(newValue)
        );

        assemblySizeSlider.valueProperty().addListener(
                (observable, oldValue, newValue)
                        -> handleSliderChangeSize(newValue)
        );

        conservationLevelOfDamageChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<String>) (observable, oldValue, newValue)
                        -> handleChoiceBoxChangeLevelOfDamage(newValue)
        );

        materialChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<String>) (observable, oldValue, newValue)
                        -> handleChoiceBoxChangeMaterial()
        );

        specializationChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<String>) (observable, oldValue, newValue)
                        -> handleChoiceBoxChangeSpecialization(newValue)
        );
    }

    private void setChoiceBoxData(){
        customerTypeChoiceBox.getItems().addAll("Retail customer", "Wholesale customer");
        serviceChoiceBox.getItems().addAll("Assembly", "Conservation");
        materialChoiceBox.getItems().addAll("Wood material", "Wood like material");
        specializationChoiceBox.getItems().addAll("Assembly", "Conservation");
        customerChoiceBox.getItems().addAll(Stream.concat(
                        retailCustomers.stream().map(customer -> customer.getFirstName() + " " + customer.getLastName()),
                        wholesaleCustomers.stream().map(customer -> customer.getCompanyName()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }

    private void handleChoiceBoxChangeCustomerType(String newValue){
        switch (newValue) {
            case "Retail customer" -> {
                loadTable(List.of("First name", "Last name", "Date of birth", "Date joined",
                        "Payment preference", "Contact preference", "Telephone", "Email", "Loyalty card level"), tableViewCustomer, retailCustomers);
                tableViewCustomer.setItems(retailCustomers);
            }
            case "Wholesale customer" -> {
                loadTable(List.of("Company name", "NIP", "Date joined", "" +
                        "Payment preference", "Contact preference", "Telephone", "Email"), tableViewCustomer, wholesaleCustomers);
                tableViewCustomer.setItems(wholesaleCustomers);
            }
        }
    }

    @FXML
    private void selectCustomer(){
        switch (customerTypeChoiceBox.getValue().toString()){
            case "Retail customer" -> {
                RetailCustomer selectedCustomer = (RetailCustomer) tableViewCustomer.getSelectionModel().getSelectedItem();
                assigmentCustomer(selectedCustomer, selectedCustomer.getFirstName() + " " + selectedCustomer.getLastName());
            }
            case "Wholesale customer" -> {
                WholesaleCustomer selectedCustomer = (WholesaleCustomer) tableViewCustomer.getSelectionModel().getSelectedItem();
                assigmentCustomer(selectedCustomer, selectedCustomer.getCompanyName());
            }
        }
    }

    private void assigmentCustomer(Customer customer, String name){
        newOrder.removeCustomer();
        newOrder.addCustomer(customer);

        selectedCustomerText.setText(name);
        saveCustomer.setVisible(true);
    }

    @FXML
    private void saveAssignedCustomer(){
        loadDataService();

        saveCustomer.setVisible(false);
        tableViewCustomer.setVisible(false);
        customerTypeChoiceBox.setDisable(true);

        serviceTypeText.setVisible(true);
        serviceChoiceBox.setVisible(true);

        selectedAssociates.setDisable(false);
        removeAssociate.setVisible(true);

        saveService.setVisible(true);
    }

    private void handleChoiceBoxChangeService(String selectedValue) {
        if ("Assembly".equals(selectedValue)) {
            changeService(false, "Assembly");
        } else if ("Conservation".equals(selectedValue)) {
            changeService(true, "Conservation");
        }
    }

    private void changeService(boolean isConservation, String title){
        conservationLevelOfDamageText.setVisible(isConservation);
        conservationLevelOfDamageChoiceBox.setVisible(isConservation);

        assemblyProductNameText.setVisible(!isConservation);
        assemblySizeText.setVisible(!isConservation);
        assemblyProductNameTextField.setVisible(!isConservation);
        assemblyProductNameTextField.setText("");
        assemblySizeSlider.setVisible(!isConservation);
        assemblySizeSlider.setValue(5.0);
        assemblySizeLabel.setVisible(!isConservation);

        materialText.setVisible(!isConservation);
        materialChoiceBox.setVisible(!isConservation);
        materialChoiceBox.getSelectionModel().select(0);

        if(isConservation){
            conservationLevelOfDamageChoiceBox.getItems().setAll("low", "high");
            loadTable(List.of("Name", "Toxicity level", "Price"), tableViewElement, chemicals);
        } else {
            handleChoiceBoxChangeMaterial();
        }

        selectedAssociates.getItems().clear();

        selectedServiceText.setText(title);

        tableViewElement.setVisible(true);
        saveService.setDisable(true);
        createNewService();
    }

    private void createNewService(){
        switch (serviceChoiceBox.getValue().toString()) {
            case "Assembly" ->
                newService = new Assembly(assemblyServices.size() + conservationServices.size(),
                        assemblyProductNameTextField.getText(),
                        (int) assemblySizeSlider.getValue());
            case "Conservation" ->
                newService = new Conservation(assemblyServices.size() + conservationServices.size(),
                        Conservation.LevelOfDamage.low);
        }
    }

    private void handleTextFieldChangeProductName(String newValue) {
        saveService.setDisable(newValue.isEmpty());
        Assembly assembly = (Assembly) newService;
        assembly.setProductName(newValue);
    }

    private void handleSliderChangeSize(Number newValue) {
        assemblySizeLabel.setText(String.valueOf(newValue.intValue()));
        Assembly assembly = (Assembly) newService;
        assembly.setSize(newValue.intValue());
    }

    private void handleChoiceBoxChangeLevelOfDamage(String newValue) {
        saveService.setDisable(newValue == null || newValue.isEmpty());
        if(conservationLevelOfDamageChoiceBox.getValue() != null){
            Conservation conservation = (Conservation) newService;
            conservation.setLevelOfDamage(Conservation.LevelOfDamage.valueOf(newValue));
        }
    }

    private void  handleChoiceBoxChangeMaterial(){
        if(materialChoiceBox.getValue() != null) {
            switch (materialChoiceBox.getValue().toString()) {
                case "Wood material" -> {
                    loadTable(List.of("Wood type", "Hardness", "Price"), tableViewElement, woodMaterials);
                    tableViewElement.setVisible(true);
                }
                case "Wood like material" -> {
                    loadTable(List.of("Material", "Manufacturer", "Price"), tableViewElement, woodLikeMaterials);
                    tableViewElement.setVisible(true);
                }
            }
        }
    }

    @FXML
    private void selectElement(){
        switch(serviceChoiceBox.getValue().toString()) {
            case "Assembly" -> {
                if (materialChoiceBox.getValue() != null) {
                    switch (materialChoiceBox.getValue().toString()) {
                        case "Wood material" -> {
                            WoodMaterial selectedMaterial = (WoodMaterial) tableViewElement.getSelectionModel().getSelectedItem();

                            addMaterialToService(selectedMaterial);
                            addElementToChoiceBox(selectedMaterial.getWoodType());
                        }
                        case "Wood like material" -> {
                            WoodLikeMaterial selectedMaterial = (WoodLikeMaterial) tableViewElement.getSelectionModel().getSelectedItem();

                            addMaterialToService(selectedMaterial);
                            addElementToChoiceBox(selectedMaterial.getMaterial());
                        }
                    }
                }
            }
            case "Conservation" -> {
                Chemical selectedChemical = (Chemical) tableViewElement.getSelectionModel().getSelectedItem();

                Conservation conservation = (Conservation) newService;
                conservation.addChemical(selectedChemical);

                addElementToChoiceBox(selectedChemical.getName());
            }
        }
    }

    private void addMaterialToService(Material material){
        Assembly assembly = (Assembly) newService;
        assembly.addMaterial(material);
    }

    private void addElementToChoiceBox(String name){
        if (!selectedAssociates.getItems().contains(name)) {
            selectedAssociates.getItems().add(name);
        }
    }

    @FXML
    private void removeAssociate(){
        if(selectedAssociates.getValue() != null){
            switch(serviceChoiceBox.getValue().toString()){
                case "Assembly" -> {
                    Assembly assembly = (Assembly) newService;
                    long idSelectedMaterial = Stream.concat(
                            woodMaterials.stream()
                                    .filter(material -> material.getWoodType().equals(selectedAssociates.getValue().toString()))
                                    .map(WoodMaterial::getId),
                            woodLikeMaterials.stream()
                                    .filter(material -> material.getMaterial().equals(selectedAssociates.getValue().toString()))
                                    .map(WoodLikeMaterial::getId)
                    ).findFirst().get();

                    assembly.removeMaterial(assembly.getMaterials().stream().filter(m -> m.getId() == idSelectedMaterial).findFirst().get());
                }
                case "Conservation" -> {
                    Conservation conservation = (Conservation) newService;
                    long idSelectedChemical = chemicals.stream()
                            .filter(chemical -> chemical.getName().equals(selectedAssociates.getValue().toString()))
                            .map(Chemical::getId).findFirst().get();

                    conservation.removeChemical(conservation.getChemicalList().stream().filter(ch -> ch.getId() == idSelectedChemical).findFirst().get());
                }
            }

            selectedAssociates.getItems().remove(selectedAssociates.getSelectionModel().getSelectedIndex());
            selectedAssociates.getSelectionModel().clearSelection();
            selectedAssociates.getSelectionModel().select(null);
        }
    }

    @FXML
    private void saveAssignedService(){
        loadDataEmployee();
        addNewServiceToDatabase();

        materialText.setVisible(false);
        materialChoiceBox.setVisible(false);
        tableViewElement.setVisible(false);
        removeAssociate.setVisible(false);

        serviceChoiceBox.setDisable(true);
        saveService.setVisible(false);

        tableViewEmployee.setVisible(true);
        specializationText.setVisible(true);
        specializationChoiceBox.setVisible(true);

        loadTable(List.of("First name", "Last name", "Date of birth", "Age", "Employment date", "Tenure"), tableViewEmployee, employees);
    }

    private void handleChoiceBoxChangeSpecialization(String value){
        ObservableList<Employee> data = FXCollections.observableArrayList( employees.stream()
                .filter(employee -> employee.getLicenses().stream().anyMatch(license -> license.getSpecialization().getCategory()
                        .equals(Specialization.CategorySpecialization.valueOf(value))))
                .collect(Collectors.toList())
        );

        loadTable(List.of("First name", "Last name", "Age", "Date of birth", "Employment date", "Tenure"), tableViewEmployee, data);
    }

    @FXML
    private void selectEmployee(){
        Employee selectedEmployee = (Employee) tableViewEmployee.getSelectionModel().getSelectedItem();
        assigmentEmployee(selectedEmployee, selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName());
    }

    private void assigmentEmployee(Employee employee, String name){
        if(checkLicense(employee)) {
            newOrder.removeEmployee();
            newOrder.addEmployee(employee);

            selectedEmployeeText.setText(name);
            saveEmployee.setVisible(true);
        } else {
            alertMessage("Employee cannot be assigned",
                    "the selected employee does not have a license in the selected type of service");
            saveEmployee.setVisible(false);
        }
    }

    private boolean checkLicense(Employee employee){
        return employee.getLicenses().stream()
                .anyMatch(license -> license.getSpecialization().getCategory()
                        .equals(Specialization.CategorySpecialization.valueOf(serviceChoiceBox.getValue().toString())));
    }

    @FXML
    private void saveEmployee(){
        saveEmployee.setVisible(false);
        specializationChoiceBox.setDisable(true);
        tableViewEmployee.setDisable(true);

        addServiceToOrder();
        addNewOrderToDatabase();
        alertMessage("Added a new order",
                "Discount: " + newOrder.getCustomer().getDiscount() + "%\nTotal price: " + newOrder.getTotalPrice() + "$");

        Stage stage = (Stage) saveEmployee.getScene().getWindow();
        stage.close();
    }

    private void addServiceToOrder(){
        newOrder.removeService();
        newOrder.addService(newService);
    }

    private void handleChoiceBoxChangeCustomer(String newValue) {
        long idSelectedCustomer = Stream.concat(
                retailCustomers.stream()
                        .filter(customer -> customer.getFirstName().equals(newValue.split(" ")[0]) && customer.getLastName().equals(newValue.split(" ")[1]))
                        .map(RetailCustomer::getId),
                wholesaleCustomers.stream()
                        .filter(customer -> customer.getCompanyName().equals(newValue))
                        .map(WholesaleCustomer::getIdC)
        ).findFirst().get();

        ObservableList<ServiceOrder> data = FXCollections.observableArrayList(serviceOrders.stream()
                .filter(order -> order.getCustomer().getIdC() == (idSelectedCustomer))
                .collect(Collectors.toList())
        );

        loadTable(List.of("Date","Start date","End date", "Total price", "Status"), tableViewOrder, data);
    }

    @FXML
    private void viewOrders(){
        newOrderView.setVisible(false);
        ordersView.setVisible(true);
    }

    @FXML
    private void returnButton(){
        ordersView.setVisible(false);
        newOrderView.setVisible(true);
    }

    private void alertMessage(String header, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("JOINERY");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();

    }
    private void loadTable(List<String> columns, TableView tableView, ObservableList<?> items){
        List<TableColumn> tableColumns = new ArrayList<>();

        for(String col : columns)
            tableColumns.add(new TableColumn(col));


        tableView.getColumns().setAll(tableColumns);

        for(TableColumn col : tableColumns) {
            float width = (float) ((tableView.widthProperty().floatValue() - 5) / tableColumns.size());
            col.setPrefWidth(width);
            col.setCellValueFactory(new PropertyValueFactory<>(Arrays.stream(col.getText().split(" "))
                    .map(String::toLowerCase)
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.joining(""))));
        }

        tableView.setItems(items);
    }
    private void loadDataCustomer(){
        session  = sessionFactory.openSession();
        try {
            session.beginTransaction();

            List<ServiceOrder> serviceOrders = session.createQuery("FROM ServiceOrder ", ServiceOrder.class).getResultList();


            List<RetailCustomer> rCustomers = session.createQuery("FROM RetailCustomer ", RetailCustomer.class).getResultList();
            List<WholesaleCustomer> wCustomers = session.createQuery("FROM WholesaleCustomer ", WholesaleCustomer.class).getResultList();

            for(ServiceOrder order : serviceOrders) {
                ServiceOrder serviceOrder = new ServiceOrder(
                        order.getId(),
                        order.getStatus(),
                        order.getStartDate()
                );
                serviceOrder.setCustomer(order.getCustomer());
                serviceOrder.setService(order.getService());
                serviceOrder.setEmployee(order.getEmployee());
                this.serviceOrders.add(serviceOrder);
            }


            for(RetailCustomer customer : rCustomers){
                this.retailCustomers.add(new RetailCustomer(
                        customer.getIdC(),
                        customer.getFirstName(),
                        customer.getLastName(),
                        customer.getDateOfBirth(),
                        customer.getDateJoined(),
                        customer.getPaymentPreference(),
                        customer.getContactPreference(),
                        customer.getTelephone(),
                        customer.getEmail(),
                        customer.getLoyaltyCardLevel()
                ));
            }

            for(WholesaleCustomer customer : wCustomers){
                this.wholesaleCustomers.add(new WholesaleCustomer(
                        customer.getIdC(),
                        customer.getCompanyName(),
                        customer.getNip(),
                        customer.getDateJoined(),
                        customer.getPaymentPreference(),
                        customer.getContactPreference(),
                        customer.getTelephone(),
                        customer.getEmail()
                ));
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        }
    }
    private void loadDataService(){
        try {
            session.beginTransaction();

            List<Assembly> aServices = session.createQuery("FROM Assembly ", Assembly.class).getResultList();
            List<Conservation> cServices = session.createQuery("FROM Conservation ", Conservation.class).getResultList();
            List<WoodMaterial>  wMaterials = session.createQuery("FROM WoodMaterial ", WoodMaterial.class).getResultList();
            List<WoodLikeMaterial> wlMaterials = session.createQuery("FROM WoodLikeMaterial ", WoodLikeMaterial.class).getResultList();
            List<Chemical> chemicals = session.createQuery("FROM Chemical ", Chemical.class).getResultList();

            for(Assembly service : aServices){
                this.assemblyServices.add(new Assembly(
                        service.getId(),
                        service.getProductName(),
                        service.getSize()
                ));
            }

            for(Conservation service : cServices){
                this.conservationServices.add(new Conservation(
                        service.getId(),
                        service.getLevelOfDamage()
                ));
            }

            for(WoodMaterial material : wMaterials){
                this.woodMaterials.add(new WoodMaterial(
                        material.getId(),
                        material.getWoodType(),
                        material.getHardness(),
                        material.getPrice()
                ));
            }

            for(WoodLikeMaterial material : wlMaterials){
                this.woodLikeMaterials.add(new WoodLikeMaterial(
                        material.getId(),
                        material.getMaterial(),
                        material.getManufacturer(),
                        material.getPrice()
                ));
            }

            for(Chemical chemical : chemicals){
                this.chemicals.add(new Chemical(
                        chemical.getId(),
                        chemical.getName(),
                        chemical.getToxicityLevel(),
                        chemical.getPrice()
                ));
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        }
    }
    private void loadDataEmployee(){
        try {
            session.beginTransaction();

            List<Specialization> specializations = session.createQuery("FROM Specialization ", Specialization.class).getResultList();
            List<Employee> employees = session.createQuery("FROM Employee ", Employee.class).getResultList();

            for(Specialization specialization : specializations){
                Specialization spec = new Specialization(
                        specialization.getId(),
                        specialization.getName(),
                        specialization.getCategory()
                );
                spec.setLicenses(spec.getLicenses());
                this.specializations.add(spec);
            }

            for(Employee employee : employees){
                Employee emp = new Employee(
                        employee.getId(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getDateOfBirth(),
                        employee.getEmploymentDate()
                );
                emp.setLicenses(employee.getLicenses());
                this.employees.add(emp);
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        }
    }
    private void addNewServiceToDatabase() {
        try {
            session.beginTransaction();

            session.save(newService);

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        }
    }
    private void addNewOrderToDatabase(){
        System.out.println(newOrder.toString());
        try {
            session.beginTransaction();

            session.save(newOrder);

            session.getTransaction().commit();
            session.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (sessionFactory != null) {
                sessionFactory.close();
            }
        }
    }
}