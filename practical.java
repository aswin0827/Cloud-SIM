import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class practical {
    public static void main(String[] args) {
        try {
            // Initialize CloudSim
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;
            CloudSim.init(numUsers, calendar, traceFlag);

            // Create Datacenter
            Datacenter datacenter = createDatacenter("Datacenter_0");

            // Create Broker
            DatacenterBroker broker = new DatacenterBroker("Broker_0");
            int brokerId = broker.getId();

            // Create VMs
            List<Vm> vms = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                int mips = 500 + i * 100;
                vms.add(new Vm(i, brokerId, mips, 1, 512, 1000, 10000, "Xen", new CloudletSchedulerSpaceShared()));
            }
            broker.submitVmList(vms);

            // Create Cloudlets
            List<Cloudlet> cloudlets = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                long length = 1000 + i * 200;
                int pesNumber = 1;
                long fileSize = 300;
                long outputSize = 300;
                Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, new UtilizationModelFull(),
                        new UtilizationModelFull(), new UtilizationModelFull());
                cloudlet.setUserId(brokerId);
                cloudlets.add(cloudlet);
            }
            broker.submitCloudletList(cloudlets);

            // Start Simulation with Space-Shared VM scheduler
            CloudSim.startSimulation();
            List<Cloudlet> spaceSharedResults = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            printCloudletResults(spaceSharedResults, "Space-Shared VM Scheduler");

            // Modify VM scheduler to Time-Shared
            for (Vm vm : vms) {
                vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
            }
            broker.submitVmList(vms);
            broker.submitCloudletList(cloudlets);

            CloudSim.startSimulation();
            List<Cloudlet> timeSharedResults = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            printCloudletResults(timeSharedResults, "Time-Shared VM Scheduler");

            // Implement priority-based scheduling (Example)
            priorityBasedScheduling(cloudlets, broker);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenter(String name) throws Exception {
        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int hostId = i;
            int ram = 2048; // MB
            long storage = 1000000; // MB
            int bw = 10000; // Mbps
            int mips = 1000;

            List<Pe> peList = new ArrayList<>();
            peList.add(new Pe(0, new PeProvisionerSimple(mips)));

            hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList,
                    new VmSchedulerSpaceShared(peList)));
        }

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
                cost, costPerMem, costPerStorage, costPerBw);

        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
    }

    private static void printCloudletResults(List<Cloudlet> list, String title) {
        System.out.println("========== " + title + " ==========");
        for (Cloudlet cloudlet : list) {
            System.out.printf("Cloudlet %d: Status: %s, Start Time: %.2f, Finish Time: %.2f\n",
                    cloudlet.getCloudletId(),
                    cloudlet.getStatus() == Cloudlet.SUCCESS ? "SUCCESS" : "FAILED",
                    cloudlet.getExecStartTime(),
                    cloudlet.getFinishTime());
        }
    }

    private static void priorityBasedScheduling(List<Cloudlet> cloudlets, DatacenterBroker broker) {
        cloudlets.sort((c1, c2) -> Long.compare(c2.getCloudletLength(), c1.getCloudletLength())); // Higher length -> Higher priority
        broker.submitCloudletList(cloudlets);
        CloudSim.startSimulation();
        List<Cloudlet> results = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();
        printCloudletResults(results, "Priority-Based Scheduling");
    }
}
