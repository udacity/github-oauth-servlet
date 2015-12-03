package com.udacity.github.oauth.test

import org.scalatest.FunSuite
import com.udacity.github.oauth.Validation
import com.udacity.github.oauth.Valid
import com.udacity.github.oauth.Invalid

class ValidationTests extends FunSuite {

  val square: Int => Int = x => x * x

  val squareV: Validation[Int => Int] = Valid(square)
  val squareI: Validation[Int => Int] = Invalid(List("not square"))

  val xV: Validation[Int] = Valid(6)
  val xI: Validation[Int] = Invalid(List("not six"))

  test("Valid#ap(Invalid)") {
    assert(xV.ap(squareI) === Invalid(List("not square")))
  }

  test("Valid#ap(Valid)") {
    assert(xV.ap(squareV) === Valid(36))
  }

  test("Invalid#ap(Valid)") {
    assert(xI.ap(squareV) === Invalid(List("not six")))
  }

  test("Invalid#ap(Invalid)") {
    assert(xI.ap(squareI) === Invalid(List("not six", "not square")))
  }

}
