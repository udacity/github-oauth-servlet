package com.udacity.github.oauth.test

import org.scalatest.FunSuite
import com.udacity.github.oauth.Validation
import com.udacity.github.oauth.Validation._
import com.udacity.github.oauth.Valid
import com.udacity.github.oauth.Invalid

class ValidationTests extends FunSuite {

  val square: Int => Int = x => x * x

  val squareV: Validation[Int => Int] = Valid(square)
  val squareI: Validation[Int => Int] = Invalid(List("not square"))

  val xV: Validation[Int] = Valid(6)
  val xI: Validation[Int] = Invalid(List("not six"))

  test("Valid#map(f)") {
    assert(xV.map(square) === Valid(36))
    assert(square <%> xV === Valid(36))
  }

  test("Invalid#map(f)") {
    assert(xI.map(square) === Invalid(List("not six")))
    assert(square <%> xI === Invalid(List("not six")))
  }

  test("Valid#ap(Invalid)") {
    assert(xV.ap(squareI) === Invalid(List("not square")))
    assert(squareI <*> xV === Invalid(List("not square")))
  }

  test("Valid#ap(Valid)") {
    assert(xV.ap(squareV) === Valid(36))
    assert(squareV <*> xV === Valid(36))
  }

  test("Invalid#ap(Valid)") {
    assert(xI.ap(squareV) === Invalid(List("not six")))
    assert(squareV <*> xI === Invalid(List("not six")))
  }

  test("Invalid#ap(Invalid)") {
    assert(xI.ap(squareI) === Invalid(List("not six", "not square")))
    assert(squareI <*> xI === Invalid(List("not six", "not square")))
  }

}
