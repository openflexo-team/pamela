package org.openflexo.pamela.test.tests1;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.CloneableProxyObject;
import org.openflexo.pamela.DeletableProxyObject;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Parameter;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;

@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoModelObjectImpl.class)
public interface TestModelObject extends AccessibleProxyObject, DeletableProxyObject, CloneableProxyObject {

	public static final String FLEXO_ID = "flexoId";
	public static final String NAME = "name";
	public static final String DELETED = "deleted";

	@Initializer
	public TestModelObject init();

	@Initializer
	public TestModelObject init(@Parameter(FLEXO_ID) String flexoId);

	// @XMLProperty(id="flexoId",kind=Kind.GETTER,xml="flexoID",defaultValue="00000")

	@Getter(value = FLEXO_ID, defaultValue = "0000")
	@XMLAttribute(xmlTag = FLEXO_ID)
	public String getFlexoID();

	// @XMLProperty(id="flexoId",kind=Kind.SETTER)
	@Setter(FLEXO_ID)
	public void setFlexoID(String flexoID);

	@Getter(value = NAME, defaultValue = "???")
	@XMLAttribute(xmlTag = NAME)
	// @CloningStrategy(StrategyType.FACTORY, factory = "deriveName()")
	@CloningStrategy(StrategyType.CLONE)
	public String getName();

	@Setter(NAME)
	public void setName(String name);

	public String deriveName();

	/*@XMLProperty(xml="lastUpdateDate")
	public Date getLastUpdateDate();
	public void setLastUpdateDate(Date lastUpdateDate);

	@XMLRelationship(xml="father",target=FlexoModelObject.class, cardinality=Cardinality.MANY_TO_ONE, reverseMethodName="children")
	public FlexoModelObject getFather();
	public void setFather(FlexoModelObject father);

	@XMLRelationship(xml="children",target=FlexoModelObject.class, cardinality=Cardinality.ONE_TO_MANY, reverseMethodName="father")
	public List<FlexoModelObject> getChildren();
	public void setChildren();*/

}
