/**
 * The interfaces in this package are marker interfaces that name the functional roles of a recurring pattern used to decompose 
 * the software found in this project.
 * 
 * The abstractions are meant to facilitate systems designed for modular component-oriented decomposition.  Components rarely
 * collaborate directly with their peers, but coupling is not always so loose as to be strictly anonymous either.  Collaboration
 * typically occurs through an EventBus when the interactions involve 1-to-many broadcast.  For collaborations that require 
 * transient one-on-one interactions, dialogue is usually brokered by a shared container artifact that serves as mediator.
 * 
 * The model also caters to a design consideration that anticipates the likelihood that some components will require a degree
 * of extension semantics and make frequent use of "Adapter", "Bridge", and "Strategy" design patterns in their composition.
 * 
 * Software developed in this context often requires a developer find patterns for recurring conversational transactions between
 * three distinct roles:
 * 
 * -- Containers that provides services to its contained components in some cases, and provides services to those
 * contained children in others.  Containers are typically protected from the details of any extension semantics exposed by
 * their children as an intentional design goal that uses the term "leaking abstraction" to describe implementations that behave 
 * otherwise.  In theory, the container concept can nest recursively.  It could be argued that the relationship between a parent
 * components and its immediate children relative to those children and their extension delegates is already a form of nested
 * Containers.
 * 
 * -- Components that encapsulate an isolated "part" of a modularly decomposed system.  There is no single API hat all Components
 * implement--the term is more of a marker than a true interface.  Even in this package, the abstractions provided are here more
 * to help developers recognize "stylistic" qualities of software that applies its terminology, rather than anything specific 
 * about its functional traits.  Components run the risk of suffering the complexities of being "middle children", as they
 * exist in between containers that provide them with services and sometimes impose constraints on how they operate at runtime,
 * and also are themselves authoritative for extension delegates, such as adapters, strategies, and bridge delegates.
 * 
 * -- Drivers are the third tier, and represent code designed according to some SDK layer exposed by a Component for any of a 
 * number of valid reasons.  Drivers are not isolated in ivory towers, but there is inherently a weaker trust relationship between
 * a component and its drivers than there is between a container and its components.  Even when a driver is developed by the same
 * team that owns its SDK's Component as well, Drivers are meant to be supported without reliance on too much trust.
 * 
 * It is often the case that a Driver will use its Component to broker interactions on its behalf.  A common example involves 
 * a Driver use case that will lead to an EventBus broadcast being sent.  It is considered a design mistake to for a component to
 * provide access to the EventBus by passing it directly by reference or expecting Dependency Injection to do so for any of its 
 * Drivers.  Instead, the mandated best practice requirement calls for a Component to provide its Driver with an adapter around
 * the Event Bus that exposes just enough functionality to satisfy the functional requirement, and also provides the Component 
 * an opportunity to sanity check any input provided through that interface by the Driver.
 * 
 * So Components often need to provide interfaces for the Drivers in their extension layer.  There are also interactions in the
 * other direction which involve Components initiating a call sequence into Driver code through a well defined interface.  The 
 * relationship between a Component and its Driver will often involve both kinds of interactions.  From a Dependency Injection 
 * perspective, this creates a quandry where the dependencies appear circular at first glance...
 * 
 * As a first step in untangling such knots, this p
 */
package info.jchein.mesosphere.domain.subsystem;