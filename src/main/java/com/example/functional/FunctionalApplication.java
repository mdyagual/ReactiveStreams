package com.example.functional;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.json.JsonObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import java.util.stream.Collectors;
import java.util.stream.Stream;

class Restaurant{
	String resName;
	String gradeScored;
	Integer score;
	String address;

	Restaurant (String resName, String gradeScored){
		this.resName = resName;
		this.gradeScored = gradeScored;
	}

	Restaurant (String resName, String gradeScored, Integer score, String address){
		this.resName = resName;
		this.gradeScored = gradeScored;
		this.score= score;
		this.address = address;
	}

	@Override
	public String toString() {
		return "Restaurant{" +
				"resName='" + resName + '\'' +
				", gradeScored='" + gradeScored + '\'' +
				", score=" + score +
				", address='" + address + '\'' +
				'}';
	}
}

@SpringBootApplication
public class FunctionalApplication {


	public static void main(String[] args) {
		ArrayList<Document> dataRestaurants = database();

		System.out.println("\n\n------OUTPUT------------\n\n");
		/* Use this line to remember how the data looks like
		dataRestaurants.stream().map(Document::toJson).limit(5).forEach(System.out::println);*/


		//1: Get the boroughs that start with letter 'B'
		boroughtsStartByB(dataRestaurants);

		//2: Get restaurants that have as cuisine 'American'
		americanCuisine(dataRestaurants);

		//TO DO
		/*3: Get the amount of restaurants whose name is just one word
		Keep it in mind that a restaurant e.g McDonals have some locals in different directions and also some records hasn't names assigned
		HINT: Remember that if the restaurant's name has spaces that means it has more than 1 word*/
		namesWith1Word(dataRestaurants);

		/*4: Get all the restaurants that received grade C in the most recent data
		HINT: The recent score is always the first one inside the list of the key "grades".*/
		restaurantsGradeC(dataRestaurants);


		/*5: Sort all the restaurants by the grade that has received in the most recent date. If the are not receiving a grade yet (grade=Not Yet Graded), ignore them.
		* HINT: Consider create a small Restaurant object with the data that you need to archive this exercise*/
		sortRestaurantsByGrade(dataRestaurants);


		//6: Get the restaurant with B grade with the highest score in the most recent date
		highestScoreGrade(dataRestaurants);

		/*7 (Optional): Investigate zip function (import org.springframework.data.util.StreamUtils;) to generate a list of strings with the next elements:
		-A stream that contains all the names of the restaurant
		-Another stream that contains the amount of words used on the restaurants's name
		Output expected
		xxxxx has 1 word
		xxxx yyyyy zz have 3 words
		xxxx yyyy have 2 words
		.
		.
		If you can solved it and if in the calculator activity you don't complete/implement the division operation, this will get considerer.
		*/



	}
	public static ArrayList<Document> database(){
		//I stablish the connection
		MongoClient mongoClient = MongoClients.create("mongodb+srv://mdyagual:mdyagual@clusterferreteria.aum6z.mongodb.net/?retryWrites=true&w=majority");
		// I'm getting the table/collection that i want to use
		MongoCollection<Document> collection = mongoClient.getDatabase("sample_restaurants").getCollection("restaurants");
		//I'm saving all the data from the previous line into an ArrayList of Document
		return collection.find().into(new ArrayList<>());
	}
	//1
	public static void boroughtsStartByB(ArrayList<Document> dataRestaurants){
		//First we define the condition to use on filter
		Predicate<Document> boroughWithB = (restaurant -> restaurant.get("borough").toString().startsWith("B"));
		//Second we define a function that will received the 'database' and will return a Stream of documents
		Function<ArrayList<Document>, Stream<Document>> getBoroughs = (dbRest) -> dbRest.stream()
				.filter(r -> boroughWithB.test(r));
		//Finally show the result
		Consumer<Stream<Document>> resultF1 = (value) -> value.forEach(r -> System.out.println(r.get("name")+": "+r.get("borough")));
		System.out.println("Filter #1");

		resultF1.accept(getBoroughs.apply(dataRestaurants));
	}
	//2
	public static void americanCuisine(ArrayList<Document> dataRestaurants){
		//First we define the condition to use on filter
		Predicate<Document> isAmerica = (data) -> data.get("cuisine").toString().equals("American");
		//Second we define a function that will received the 'database' and will return a Stream of documents
		Function<ArrayList<Document>,Stream<Document>> cuisineAmerican = (dbRest) -> dbRest.stream()
				.filter(r -> isAmerica.test(r));
		//Finally show the result
		Consumer<Stream<Document>> resultF2 = (value) -> value.forEach(r -> System.out.println( r.get("name")+"------------"+r.get("cuisine")));
		System.out.println("Filter #2");
		resultF2.accept(cuisineAmerican.apply(dataRestaurants));
	}
	//3
	public static void namesWith1Word (ArrayList<Document> dataRestaurants){
		Predicate<Document> isJustASingleWord = (d) -> !(d.getString("name").contains(" ")) && !(d.getString("name").equals(""));
		Set<String> singleName = dataRestaurants.stream()
				.filter(n -> isJustASingleWord.test(n)).collect(Collectors.toSet()).stream().map(document -> document.getString("name")).collect(Collectors.toUnmodifiableSet());
		//------------


		Consumer<Set<String>> total = (value) -> System.out.println("Total: "+value.stream().count());

		System.out.println("Filter #3");
		total.accept(singleName);
	}
	//4
	public static void restaurantsGradeC(ArrayList<Document> dataRestaurants){
		Function<Document,List<Document>> getGrade= (restaurant) -> restaurant.getList("grades",Document.class);
		System.out.println("Filter #4");
		dataRestaurants.stream().forEach(r -> {
			List<Document> grade = getGrade.apply(r);
			if(grade.stream().count()>0){
				var g = grade.get(0).getString("grade");
				if(g.equals("C"))
					System.out.println(r.getString("name")+": "+g);
			}
		});

	}
	//5
	public static void sortRestaurantsByGrade(ArrayList<Document> dataRestaurants){
		System.out.println("Name: Grade");
		Function<Document,List<Document>> getGrade2= (restaurant) -> restaurant.getList("grades",Document.class);
		List<Restaurant> restaurants = new ArrayList<>();
		dataRestaurants.stream().forEach(restaurant -> {
			var resName = restaurant.getString("name");
			var grade = getGrade2.apply(restaurant);
			if(grade.stream().count()>0){
				var g = grade.get(0).getString("grade");
				if(!g.equals("Not Yet Graded"))
					restaurants.add(new Restaurant(resName,g));

			}

		});
		List<Restaurant> ordenRestaurantes = restaurants.stream().sorted(Comparator.comparing(g -> g.gradeScored)).collect(Collectors.toList());
		System.out.println("Filter #5");
		ordenRestaurantes.forEach(System.out::println);
	}
	//6
	public static void highestScoreGrade (ArrayList<Document> dataRestaurants){
		Function<Document,List<Document>> getGrades= (restaurant) -> restaurant.getList("grades",Document.class);


		Function<Document,Boolean> bGraded=(restaurant) -> getGrades.apply(restaurant).size()>0? getGrades.apply(restaurant).get(0).get("grade").equals("B"): false;
		Predicate<Integer> changingNull = Objects::nonNull;

		List<Restaurant> restaurantsSet=dataRestaurants.stream().filter(bGraded::apply).map(r -> {

			Document restDoc = new Document();

			var name = r.getString("name");
			var grades = getGrades.apply(r);
			var address = r.get("address").toString();
			var score = grades.get(0).getInteger("score");
			restDoc.append("name",name);
			restDoc.append("grade",grades.get(0).getString("grade"));
			restDoc.append("score",changingNull.test(score)?score:-1);
			restDoc.append("street",address);

			var restaurant = new Restaurant(restDoc.getString("name"),restDoc.getString("grade"),restDoc.getInteger("score"),restDoc.getString("street"));
			return restaurant;

		}).collect(Collectors.toList());

		System.out.println("Filter #6");
		Restaurant r = restaurantsSet.stream().max(Comparator.comparingInt(res -> res.score)).get();

		System.out.println(r);


	}

	//7


}
