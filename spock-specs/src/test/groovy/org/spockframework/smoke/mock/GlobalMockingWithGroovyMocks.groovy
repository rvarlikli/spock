package org.spockframework.smoke.mock

import spock.lang.Specification
import java.lang.reflect.Modifier

class GlobalMockingWithGroovyMocks extends Specification {
  def "mock instance method"() {
    def myList = new ArrayList()
    def anyList = GroovyMock(ArrayList, global: true)

    when:
    myList.add(1)
    myList.size()

    then:
    1 * anyList.add(1)
    1 * anyList.size()
    0 * _
  }

  def "global mocking is undone before next method"() {
    when:
    new ArrayList().add(1)

    then:
    0 * _
  }

  def "calls real method if call isn't mocked"() {
    def myList = new ArrayList()
    GroovyMock(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    myList.size() == 2
  }

  def "calls real method if mocked call provides no result"() {
    def myList = new ArrayList()
    GroovyMock(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1)
    myList.size() == 2
  }

  def "does not call real method if mocked call provides result"() {
    def myList = new ArrayList()
    GroovyMock(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1) >> true
    myList.size() == 0
  }

  def "can call real method when providing result"() {
    def myList = new ArrayList()
    GroovyMock(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1) >> { callRealMethod() }
    myList.size() == 2
  }

  def "can call real method with changed arguments"() {
    def myList = new ArrayList()
    GroovyMock(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1) >> { callRealMethodWith(42) }
    myList.size() == 2
    myList[1] == 42
  }

  def "mock dynamic instance method"() {
    def anyList = GroovyMock(ArrayList, global: true)

    when:
    new ArrayList().foo(42)

    then:
    1 * anyList.foo(42) >> true
  }

  // TODO: devirtualize
  def "mock dynamic instance method called via MOP"() {
    def anyPerson = GroovyMock(Person, global: true)

    when:
    new Person().invokeMethod("foo", [42] as Object[])

    then:
    //1 * anyPerson.foo(42) >> "done"
    1 * anyPerson.invokeMethod("foo", _) >> "done"
  }

  def "mock final instance method"() {
    assert Modifier.isFinal(Person.getMethod("performFinal", String).getModifiers())

    def anyPerson = GroovyMock(Person, global: true)

    when:
    new Person().performFinal("work")

    then:
    1 * anyPerson.performFinal("work")
  }

  def "mock final class"() {
    assert Modifier.isFinal(FinalPerson.getModifiers())

    def anyPerson = GroovyMock(FinalPerson, global: true)

    when:
    new FinalPerson().perform("work")

    then:
    1 * anyPerson.perform("work")
  }

  def "mock static method"() {
    GroovyMock(Collections, global: true)

    when:
    Collections.emptyList()
    Collections.nCopies(42, "elem")

    then:
    1 * Collections.emptyList()
    1 * Collections.nCopies(42, "elem")
    0 * _
  }

  def "mock dynamic static method"() {
    GroovyMock(Collections, global: true)

    when:
    Collections.foo()
    Collections.bar(42, "elem")

    then:
    1 * Collections.foo() >> _
    1 * Collections.bar(42, "elem") >> _
    0 * _
  }

  def "mock constructor"() {
    GroovyMock(Person, global: true)

    when:
    new Person("fred", 42)
    new Person("barney", 21)

    then:
    1 * new Person("fred", 42)
    1 * new Person("barney", 21)
    0 * _
  }

  static class Person {
    String name
    int age

    String perform(String work) { "done" }
    final String performFinal(String work) { "done" }

    Person(String name = "fred", int age = 42) {
      this.name = name
      this.age = age
    }
  }

  static final class FinalPerson {
    String name
    int age

    String perform(String work) { "done" }

    FinalPerson(String name = "fred", int age = 42) {
      this.name = name
      this.age = age
    }
  }
}