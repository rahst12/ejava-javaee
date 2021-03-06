package ejava.projects.edmv.bl;

import gov.ojp.it.jxdm._3_0.AddressType;
import gov.ojp.it.jxdm._3_0.DecalType;
import gov.ojp.it.jxdm._3_0.IDType;
import gov.ojp.it.jxdm._3_0.ImageType;
import gov.ojp.it.jxdm._3_0.Person;
import gov.ojp.it.jxdm._3_0.PersonHeightMeasureType;
import gov.ojp.it.jxdm._3_0.PersonNameTextType;
import gov.ojp.it.jxdm._3_0.PersonNameType;
import gov.ojp.it.jxdm._3_0.PersonPhysicalDetailsType;
import gov.ojp.it.jxdm._3_0.PersonWeightMeasureType;
import gov.ojp.it.jxdm._3_0.ReferenceType;
import gov.ojp.it.jxdm._3_0.ResidenceType;
import gov.ojp.it.jxdm._3_0.StreetType;
import gov.ojp.it.jxdm._3_0.TextType;
import gov.ojp.it.jxdm._3_0.VehicleRegistration;
import gov.ojp.it.jxdm._3_0.VehicleType;
import gov.ojp.it.jxdm._3_0_3.proxy.ncic_2000._1_0.EYEType;
import gov.ojp.it.jxdm._3_0_3.proxy.ncic_2000._1_0.RACType;
import gov.ojp.it.jxdm._3_0_3.proxy.ncic_2000._1_0.SEXType;
import gov.ojp.it.jxdm._3_0_3.proxy.ncic_2000._1_0.VCOType;
import gov.ojp.it.jxdm._3_0_3.proxy.ncic_2000._1_0.VMAType;
import gov.ojp.it.jxdm._3_0_3.proxy.ncic_2000._1_0.VMOType;
import gov.ojp.it.jxdm._3_0_3.proxy.usps_states._1.USStateCodeType;
import gov.ojp.it.jxdm._3_0_3.proxy.xsd._1.Base64Binary;
import gov.ojp.it.jxdm._3_0_3.proxy.xsd._1.Date;
import gov.ojp.it.jxdm._3_0_3.proxy.xsd._1.GMonth;
import gov.ojp.it.jxdm._3_0_3.proxy.xsd._1.GYear;
import info.ejava.projects.edmv._1.Dmv;
import info.ejava.projects.edmv._1.PersonsType;
import info.ejava.projects.edmv._1.VehicleRegistrationsType;

import java.io.Writer;
import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ejava.projects.edmv.bo.DMVPerson;
import ejava.projects.edmv.bo.DMVPersonPhysicalDetails;
import ejava.projects.edmv.bo.DMVResidence;
import ejava.projects.edmv.bo.DMVVehicleRegistration;
import ejava.projects.edmv.dao.DMVPersonDAO;
import ejava.projects.edmv.dao.DMVVehicleDAO;

/**
 * This class will iterate of all person and vehicle registrations and 
 * then transfer the information from the OR-mapped objects to JAXB objects.
 * The JAXB object are marshalled to the supplied writer.
 * 
 * @author jcstaff
 *
 */
public class DataGen {
    private Log log = LogFactory.getLog(DataGen.class); 
    protected DMVPersonDAO personDAO;
    protected DMVVehicleDAO vehicleDAO;
    private int id = 0;
    private DatatypeFactory dtf;
    
    public void setPersonDAO(DMVPersonDAO dao) {
        this.personDAO = dao;
    }
    public void setVehicleDAO(DMVVehicleDAO dao) {
        this.vehicleDAO = dao;
    }
    
    public void generate(Writer writer) 
        throws JAXBException, DatatypeConfigurationException {
        dtf = DatatypeFactory.newInstance();
        JAXBContext jaxbc = JAXBContext.newInstance(Dmv.class);
        Marshaller m = jaxbc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        Dmv dmvDoc = new Dmv();
        dmvDoc.setPeople(new PersonsType());
        dmvDoc.setVehicleRegistrations(new VehicleRegistrationsType());
   
        //get all people
        int index=0;
        List<DMVPerson> people;
        do {
           people = personDAO.getPeople(index, 10);
           for (DMVPerson p : people) {
               dmvDoc.getPeople().getPerson().add(createXMLPerson(p));
           }           
           index += people.size();
        } while (people.size() > 0);
        log.debug("found " + index + " total people");
        
        //get all vehicle registrations
        index=0;
        List<DMVVehicleRegistration> vRegs;
        do {
            vRegs = vehicleDAO.getRegistrations(index, 10);
            for (DMVVehicleRegistration vReg : vRegs) {
                dmvDoc.getVehicleRegistrations()
                      .getVehicleRegistration()
                      .add(createXMLRegistration(vReg, dmvDoc.getPeople()));
            }
            index += vRegs.size();
        } while (vRegs.size() > 0);
        log.debug("found " + index + " total registrations");
        
        m.marshal(dmvDoc, writer);        
    }
    
    private VehicleRegistration createXMLRegistration(
            DMVVehicleRegistration reg, PersonsType people) {
        VehicleRegistration xmlReg = new VehicleRegistration();
        xmlReg.setVehicle(new VehicleType());
        xmlReg.getVehicle().setVehicleColorPrimaryCode(new VCOType());
        xmlReg.getVehicle().setVehicleID(new IDType());
        xmlReg.getVehicle().getVehicleID().setID(new TextType());
        xmlReg.getVehicle().setVehicleMakeCode(new VMAType());
        xmlReg.getVehicle().setVehicleModelCode(new VMOType());
        xmlReg.getVehicle().setVehicleModelYearDate(new GYear());
        xmlReg.getVehicle().getVehicleModelYearDate().setValue(
             dtf.newXMLGregorianCalendar());
        xmlReg.setVehicleLicensePlateID(new IDType());
        xmlReg.getVehicleLicensePlateID().setID(new TextType());
        xmlReg.setVehicleRegistrationDecal(new DecalType());
        xmlReg.getVehicleRegistrationDecal().setDecalMonthDate(new GMonth());
        xmlReg.getVehicleRegistrationDecal().getDecalMonthDate().setValue(
             dtf.newXMLGregorianCalendar());
        xmlReg.getVehicleRegistrationDecal().setDecalYearDate(new GYear());
        xmlReg.getVehicleRegistrationDecal().getDecalYearDate().setValue(
             dtf.newXMLGregorianCalendar());
     
        xmlReg.setId("vr" + ++id);
        xmlReg.setSourceIDText("" + reg.getId());
        
        xmlReg.getVehicle().getVehicleColorPrimaryCode()
                           .setValue(reg.getColor());
        xmlReg.getVehicle().getVehicleID().getID()
                           .setValue(reg.getVin());
        xmlReg.getVehicle().getVehicleMakeCode()
                           .setValue(reg.getMake());
        xmlReg.getVehicle().getVehicleModelCode()
                           .setValue(reg.getModel());
        xmlReg.getVehicle().getVehicleModelYearDate().getValue()
                           .setYear(Integer.parseInt(reg.getYear()));
        
        xmlReg.getVehicleLicensePlateID().getID()
                           .setValue(reg.getTagNo());
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(reg.getExpiration());
        xmlReg.getVehicleRegistrationDecal().getDecalMonthDate().getValue()
                           .setMonth(cal.get(GregorianCalendar.MONTH) + 1);
        xmlReg.getVehicleRegistrationDecal().getDecalYearDate().getValue()
                           .setYear(cal.get(GregorianCalendar.YEAR));
        
        log.debug("vehicle has " + reg.getOwners().size() + " owners");
        for (DMVPerson o : reg.getOwners()) {
            for (Person p : people.getPerson()) {
                if (p.getSourceIDText().equals("" + o.getId())) {
                    xmlReg.getVehicle().getPropertyOwnerPerson().add(
                            new ReferenceType(p));
                }
            }
        }
        return xmlReg;
    }
    protected Person createXMLPerson(DMVPerson person) {
        Person xmlPerson = new Person();
        PersonNameType name = new PersonNameType();
        name.setPersonGivenName(new PersonNameTextType());
        name.setPersonMiddleName(new PersonNameTextType());
        name.setPersonSurName(new PersonNameTextType());
        name.setPersonSuffixName(new TextType());        
        xmlPerson.setPersonName(name);
        xmlPerson.setPersonBirthDate(new Date());
        
        xmlPerson.setSourceIDText("" + person.getId());
        xmlPerson.setId("p" + ++id);
        name.getPersonGivenName().setValue(person.getGivenName());
        name.getPersonMiddleName().setValue(person.getMiddleName());
        name.getPersonSurName().setValue(person.getSurName());
        name.getPersonSuffixName().setValue(person.getSuffixName());
        
        java.util.Date dob = person.getPhysicalDetails().getDob();
        if (dob != null) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(dob);
            XMLGregorianCalendar xDate = dtf.newXMLGregorianCalendar();
            xDate.setYear(cal.get(GregorianCalendar.YEAR));
            xDate.setMonth(cal.get(GregorianCalendar.MONTH) + 1);
            xDate.setDay(cal.get(GregorianCalendar.DAY_OF_MONTH));
            xmlPerson.getPersonBirthDate().setValue(xDate);
        }

        xmlPerson.setPersonPhysicalDetails(
                createXMLPhysicalDetails(person.getPhysicalDetails()));
        
        for (DMVResidence residence : person.getResidences()) {
            ResidenceType xmlResidence = createXMLResidence(residence);
            xmlPerson.getResidence().add(xmlResidence);
        }
        return xmlPerson;
   }

   protected PersonPhysicalDetailsType createXMLPhysicalDetails(
           DMVPersonPhysicalDetails pd) {
       PersonPhysicalDetailsType xmlPd = new PersonPhysicalDetailsType();
       xmlPd.setPersonEyeColorCode(new EYEType());
       xmlPd.setPersonHairColorText(new TextType());
       xmlPd.setPersonHeightMeasure(new PersonHeightMeasureType());
       xmlPd.setPersonWeightMeasure(new PersonWeightMeasureType());
       xmlPd.setPersonRaceCode(new RACType());
       xmlPd.setPersonSexCode(new SEXType());
       
       xmlPd.getPersonEyeColorCode().setValue(pd.getEyeColor());
       xmlPd.getPersonHairColorText().setValue(pd.getHairColor());
       xmlPd.getPersonHeightMeasure().setValue(new BigDecimal(pd.getHeight()));
       xmlPd.getPersonWeightMeasure().setValue(new BigDecimal(pd.getWeight()));
       xmlPd.getPersonSexCode().setValue(pd.getSex());
       if (pd.getPhoto() != null) {
           xmlPd.setPersonDigitalImage(new ImageType());
           xmlPd.getPersonDigitalImage()
                .setBinaryObjectBase64(new Base64Binary());
           xmlPd.getPersonDigitalImage()
                .getBinaryObjectBase64().setValue(pd.getPhoto().getImage());
       }
       
       return xmlPd;
   }
   
   protected ResidenceType createXMLResidence(DMVResidence residence) {
       ResidenceType xmlResidence = new ResidenceType();
       xmlResidence.setResidenceStartDate(new Date());
       xmlResidence.setResidenceEndDate(new Date());
       
       GregorianCalendar gDate = new GregorianCalendar();
       XMLGregorianCalendar xDate = dtf.newXMLGregorianCalendar();
       
       if (residence.getStartDate() != null) {
           gDate.setTime(residence.getStartDate());
           xDate.setYear(gDate.get(GregorianCalendar.YEAR));
           xDate.setMonth(gDate.get(GregorianCalendar.MONTH) + 1);
           xDate.setDay(gDate.get(GregorianCalendar.DAY_OF_MONTH));
           xmlResidence.getResidenceStartDate().setValue(xDate);
       }
       
       if (residence.getEndDate() != null) {
           gDate.setTime(residence.getEndDate());
           xDate.setYear(gDate.get(GregorianCalendar.YEAR));
           xDate.setMonth(gDate.get(GregorianCalendar.MONTH) + 1);
           xDate.setDay(gDate.get(GregorianCalendar.DAY_OF_MONTH));
           xmlResidence.getResidenceEndDate().setValue(xDate);
       }
       
       if (residence.getLocation() != null) {
           AddressType address = new AddressType();
           address.setLocationStreet(new StreetType());
           address.getLocationStreet().setStreetNumberText(new TextType());
           address.getLocationStreet().setStreetName(new TextType());
           address.setLocationCityName(new TextType());
           address.setLocationStateCodeUSPostalService(new USStateCodeType());
           address.setLocationPostalCodeID(new IDType());
           address.getLocationPostalCodeID().setID(new TextType());
           
           address.getLocationStreet().getStreetNumberText().setValue(
                   residence.getLocation().getStreetNumber().trim());
           address.getLocationStreet().getStreetName().setValue(
                   residence.getLocation().getStreetName().trim());
           address.getLocationCityName().setValue(
                   residence.getLocation().getCityName().trim());
           address.getLocationStateCodeUSPostalService().setValue(
                   residence.getLocation().getState().trim());
           address.getLocationPostalCodeID().getID().setValue(
                   residence.getLocation().getZip().trim());
           
           xmlResidence.setLocationAddress(address);
       }
       
       return xmlResidence;
   }
}
