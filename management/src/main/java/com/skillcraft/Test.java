package com.skillcraft;

import java.awt.print.Pageable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;

public class Test {

	static void main() {
//		List<Integer> list = new ArrayList<Integer>();
		int[] arr = {1, 2, 1, 2, 1, 3, 2};

		System.out.println(countPairsStream(arr));

}
//There is a large pile of socks that must be paired by color.
// Given an array of integers representing the color of each sock.
//Determine how many pairs of socks with matching colors there are.
//		Example: [1,2,1,2,1,3,2]
//There is one pair of color 1 and one of color 2.
// There are three odd socks left, one of each color.
// The number of pairs is 2

public static long countPairsStream(int[] socks) {
	return Arrays.stream(socks)
			.boxed()
			.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
			.values().stream()
			.mapToLong(count -> count / 2)
			.sum();
}}

/*
Please design a REST API solution, which handles a request from a Frontend client,
and essentially save the data into relational database. The UI has the functionalitiy to retrieve
and show data from database.
The data will be saved, at DB level, in a table named PERSON.
The fields from the UI are:
- first name
- last name
- age
- address
- unique identification number (example: Social Security Number for USA)
- date of birth
Design database structure.
Design layer(s) of application(s).
Design REST operations with method, path, parameters, response*/

/**
- Tables:
Person
 - id
 - first_name
 - last_name
 - address_id
 - UIN
 - date_of_birth
		(one to many)
Adress

Controllers
Services (Person -> DTO, DTO -> Person)
Repository


 @GetMaping
 public ResponseBody<PersonDTO> getPerson(){
 }

 /users

 /{id}
 get
 /{id}
 post
 /{id}
 put
 /{id}
 delete

 /getall
 List<Page<UserDTO>> getAll()




 */
