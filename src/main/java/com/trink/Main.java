package com.trink;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.bokun.metrics.service.control.grpc.MetricsControlServiceGrpc;
import io.bokun.metrics.service.control.grpc.OutgoingRequest;
import io.bokun.metrics.service.control.grpc.OutgoingResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.intercom.api.Company;
import io.intercom.api.CompanyCollection;
import io.intercom.api.ScrollableUserCollection;
import io.intercom.api.User;

import java.io.IOException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static final void main(String[] args) throws IOException {

        System.out.println("--------- START ---------");
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("10.0.11.202", 80).usePlaintext().build();
//        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 30000).usePlaintext().build();
        MetricsControlServiceGrpc.MetricsControlServiceBlockingStub serviceStub = MetricsControlServiceGrpc.newBlockingStub(managedChannel);


        Main main = new Main();

        //vendor
        main.createVendor(serviceStub);

        //user



        System.out.println("--------- END ---------");

    }


    public void createVendor(MetricsControlServiceGrpc.MetricsControlServiceBlockingStub serviceStub) {

        OutgoingRequest.Builder builder = OutgoingRequest.newBuilder();

        //MULTIPLE IDS
        String s = "";
        for (int i=1; i<=12439;i++) {
            s+=i + ",";

        }
        String ids = s.substring(0, s.length() - 1);
        builder.setExplicitVendorIds(ids);
//        builder.setExplicitVendorIds("4913");
//        builder.setExplicitVendorIds("18927");
        //4161 - Bokun Test - used for intercom booking count update
        //builder.setExplicitVendorIds("4161");

        OutgoingRequest req = builder.build();
        Iterator<OutgoingResponse> salesforceResponseIterator = serviceStub.outgoingUpsert(req);
        OutgoingResponse next = salesforceResponseIterator.next();
    }

    public void createUser() {

    }


    private static Multimap<String, User> getIntercomUsers() {
        //get users
        System.out.println("collecting users");
        ScrollableUserCollection userScroll = User.scroll();
        Multimap<String, User> usersMultimap = ArrayListMultimap.create();
        while (userScroll != null && userScroll.getPage() != null && !userScroll.getPage().isEmpty()) {
            List<User> userPage = userScroll.getPage();
            for (User user : userPage) {
                CompanyCollection companyCollection = user.getCompanyCollection();
                if (companyCollection != null && companyCollection.getPage() != null && !companyCollection.getPage().isEmpty()) {
                    for (Company company : companyCollection.getPage()) {
                        if (company != null) {
                            System.out.println("company updated");
                            usersMultimap.put(company.getId(), user);
                        }
                    }
                }
            }
            System.out.println("added " + usersMultimap.size() + " companies in multimap so far");
            userScroll = userScroll.scroll();
        }
        System.out.println("users collected. " + usersMultimap.size());
        return usersMultimap;
    }

}
