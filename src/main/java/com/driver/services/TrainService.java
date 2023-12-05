package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import jdk.internal.util.OperatingSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train=new Train();
        String route="";
        for(Station station:trainEntryDto.getStationRoute()){
            route.concat(station.toString());
        }
        train.setRoute(route);
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        train.setBookedTickets(new ArrayList<>());

        train=trainRepository.save(train);

        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Train train=trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();

        String route=train.getRoute();
        LinkedHashMap<Station,Integer> hash=new LinkedHashMap<>();
        String dummy="";
        for(int i=0;i<route.length();i++){
            dummy+=route.charAt(i);
            String finalDummy = dummy;
            if(Arrays.stream(Station.values()).anyMatch((p) -> p.name().equals(finalDummy))){
                Station station=Station.valueOf(finalDummy);
                dummy="";
                hash.put(station,0);
            }
        }
        List<Ticket> ticketList=train.getBookedTickets();
        for(Ticket ticket:ticketList){
            Station from=ticket.getFromStation();
            Station to=ticket.getToStation();
            hash.put(from,hash.get(from)+1);
            hash.put(to,hash.get(to)-1);
        }
        int sum=0;
        for (Map.Entry<Station, Integer> mapElement : hash.entrySet()){
            sum+=mapElement.getValue();
            Station station=mapElement.getKey();
            hash.put(station,sum);
        }

       return null;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train=trainRepository.findById(trainId).get();
        String[] routes= train.getRoute().split(",",-1);
        Station station1=null;
        for(String s:routes){
            if(s.equals(station.toString()))station1=Station.valueOf(s);
        }
        if(station1==null)throw new Exception("Train is not passing from this station");
        List<Ticket> ticketList=train.getBookedTickets();
        int count=0;
        for(Ticket ticket:ticketList){
            if(ticket.getFromStation()==station)count++;
        }
        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Train train=trainRepository.findById(trainId).get();
        int age =0;
        List<Ticket> ticketList=train.getBookedTickets();
        for(Ticket ticket:ticketList) {
            List<Passenger> passengerList = ticket.getPassengersList();
            for (Passenger passenger : passengerList) {
                if (passenger.getAge() > age) {
                    age = passenger.getAge();
                }
            }
        }
        return age;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Train> trainList=trainRepository.findAll();
        List<Integer> list=new ArrayList<>();
        for(Train train:trainList){
            String[] routes=train.getRoute().split(",");
            LocalTime departureTime=train.getDepartureTime();
            for(String route:routes){
                if(route.equals(station.toString())){
                    if(!departureTime.isBefore(startTime)&&!departureTime.isAfter(endTime)){
                        list.add(train.getTrainId());
                    }
                }
                departureTime=departureTime.plusHours(1);
            }
        }
        return list;
    }

}
