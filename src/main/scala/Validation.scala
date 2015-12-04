package com.udacity.github.oauth

object Validation {

  implicit class FnCofunctor[A,B](g: A => B) {
    def <%>[Z](x: Validation[A]) = x map g
  }

  implicit class ValidationCofunctor[A,B](f: Validation[A => B]) {
    def <*>(a: Validation[A]) = a ap f
  }

}

sealed trait Validation[+A] {

  def map[B](f: A => B): Validation[B] = this match {
    case Valid(value) => Valid(f(value))
    case Invalid(errors) => Invalid(errors)
  }

  def ap[B](fV: Validation[A => B]): Validation[B] = this match {
    case Valid(value) => fV match {
      case Valid(f) => Valid(f(value))
      case Invalid(errors) => Invalid(errors)
    }
    case Invalid(errors) => fV match {
      case Valid(f) => Invalid(errors)
      case Invalid(errors2) => Invalid(errors ++ errors2)
    }
  }

}

case class Valid[A](value: A) extends Validation[A]
case class Invalid(errors: List[String]) extends Validation[Nothing]
