package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        Train train=trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        String[] routes=train.getRoute().split(",");
        boolean trainTo=false;
        boolean trainFrom=false;
        int fromIdx=0;
        int toIdx=0;
        String fromStation=bookTicketEntryDto.getFromStation().toString();
        String toStation=bookTicketEntryDto.getToStation().toString();
        for(int i=0;i<routes.length;i++){
            if(routes[i].equals(fromStation)){
                fromIdx=i;
                trainFrom=true;
            }
            if(routes[i].equals(toStation)){
                toIdx=i;
                trainTo=true;
            }
        }
        if(!trainFrom||!trainTo){
            throw new Exception("Invalid stations");
        }
        int totalSeats= train.getNoOfSeats();
        int bookedSeats=0;
        List<Ticket> ticketList=train.getBookedTickets();
        for(Ticket ticket:ticketList){
            bookedSeats+=ticket.getPassengersList().size();
        }
        int availableSeats=totalSeats-bookedSeats;
        if(availableSeats<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }
        int fair=Math.abs(fromIdx-toIdx)*300;
        Ticket ticket=new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(fair);
        List<Passenger> passengerList=new ArrayList<>();
        for(int idx: bookTicketEntryDto.getPassengerIds()){
            Passenger passenger=passengerRepository.findById(idx).get();
            passengerList.add(passenger);
        }
        ticket.setPassengersList(passengerList);
        ticketList.add(ticket);
        ticket.setTrain(train);
        train.setBookedTickets(ticketList);
        trainRepository.save(train);
       return ticket.getTicketId();

    }
}
