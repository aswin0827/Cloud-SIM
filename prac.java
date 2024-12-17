package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class VmAllocationPolicyRoundRobin extends VmAllocationPolicy {
    private int lastHostIndex;

    public VmAllocationPolicyRoundRobin(List<? extends Host> list) {
        super(list);
        lastHostIndex = -1;
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        int hostListSize = getHostList().size();
        for (int i = 0; i < hostListSize; i++) {
            lastHostIndex = (lastHostIndex + 1) % hostListSize;
            Host host = getHostList().get(lastHostIndex);
            if (host.isSuitableForVm(vm)) {
                return host.vmCreate(vm);
            }
        }
        return false;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        return host.vmCreate(vm);
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        vm.getHost().vmDestroy(vm);
    }

    @Override
    public Host getHost(Vm vm) {
        return vm.getHost();
    }

    @Override
    public Host getHost(int vmId, int userId) {
        for (Host host : getHostList()) {
            if (host.getVm(vmId, userId) != null) {
                return host;
            }
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        return null; // No optimization in this simple policy
    }
}

class VmAllocationPolicyFirstFit extends VmAllocationPolicy {
    public VmAllocationPolicyFirstFit(List<? extends Host> list) {
        super(list);
    }


    @Override
    public boolean allocateHostForVm(Vm vm) {
        for (Host host : getHostList()) {
            if (host.isSuitableForVm(vm)) {
                return host.vmCreate(vm);
            }
        }
        return false;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        return host.vmCreate(vm);
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        vm.getHost().vmDestroy(vm);
    }

    @Override
    public Host getHost(Vm vm) {
        return vm.getHost();
    }

    @Override
    public Host getHost(int vmId, int userId) {
        for (Host host : getHostList()) {
            if (host.getVm(vmId, userId) != null) {
                return host;
            }
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        return null; // No optimization in this simple policy
    }
}

class VmAllocationPolicyBestFit extends VmAllocationPolicy {
    public VmAllocationPolicyBestFit(List<? extends Host> list) {
        super(list);
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        Host bestHost = null;
        double minRemainingCapacity = Double.MAX_VALUE;

        for (Host host : getHostList()) {
            if (host.isSuitableForVm(vm)) {
                double remainingCapacity = host.getAvailableMips();
                if (remainingCapacity < minRemainingCapacity) {
                    minRemainingCapacity = remainingCapacity;
                    bestHost = host;
                }
            }
        }

        if (bestHost != null) {
            return bestHost.vmCreate(vm);
        }
        return false;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        return host.vmCreate(vm);
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        vm.getHost().vmDestroy(vm);
    }

    @Override
    public Host getHost(Vm vm) {
        return vm.getHost();
    }

    @Override
    public Host getHost(int vmId, int userId) {
        for (Host host : getHostList()) {
            if (host.getVm(vmId, userId) != null) {
                return host;
            }
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        return null; // No optimization in this simple policy
    }
}

public class prac {
    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmlist;

    private static List<Vm> createVM(int userId, int vms, int idShift) {
        LinkedList<Vm> list = new LinkedList<>();

        long size = 10000;
        int ram = 512;
        int mips = 250;
        long bw = 1000;
        int pesNumber = 1;
        String vmm = "Xen";

        Vm[] vm = new Vm[vms];

        for (int i = 0; i < vms; i++) {
            vm[i] = new Vm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            list.add(vm[i]);
        }

        return list;
    }

    private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift) {
        LinkedList<Cloudlet> list = new LinkedList<>();

        long length = 40000;
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlet = new Cloudlet[cloudlets];

        for (int i = 0; i < cloudlets; i++) {
            cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }

    public static void main(String[] args) {
        Log.printLine("Starting CloudSimExample with Performance Metrics...");

        try {
            int num_user = 2;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;

            CloudSim.init(num_user, calendar, trace_flag);

            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            Datacenter datacenter1 = createDatacenter("Datacenter_1");

            DatacenterBroker broker = createBroker("Broker_0");
            int brokerId = broker.getId();

            vmlist = createVM(brokerId, 5, 0);
            cloudletList = createCloudlet(brokerId, 10, 0);

            broker.submitVmList(vmlist);
            broker.submitCloudletList(cloudletList);

            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(newList);
            calculatePerformanceMetrics(newList, vmlist);

            Log.printLine("CloudSimExample finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();

        List<Pe> peList1 = new ArrayList<>();
        int mips = 1000;
        peList1.add(new Pe(0, new PeProvisionerSimple(mips)));
        peList1.add(new Pe(1, new PeProvisionerSimple(mips + 8000)));
        peList1.add(new Pe(2, new PeProvisionerSimple(mips + 2000)));
        peList1.add(new Pe(3, new PeProvisionerSimple(mips + 3000)));

        List<Pe> peList2 = new ArrayList<>();
        peList2.add(new Pe(0, new PeProvisionerSimple(mips + 4000)));
        peList2.add(new Pe(1, new PeProvisionerSimple(mips + 500)));

        int hostId = 0;
        int ram = 16384;
        long storage = 1000000;
        int bw = 10000;

        hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1, new VmSchedulerTimeShared(peList1)));
        hostId++;
        hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList2, new VmSchedulerTimeShared(peList2)));

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.1;
        double costPerBw = 0.1;
        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyRoundRobin(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker(String name) {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() + indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }

    private static void calculatePerformanceMetrics(List<Cloudlet> cloudletList, List<Vm> vmList) {
        double totalExecutionTime = 0.0;
        double totalCost = 0.0;
        double totalCpuUtilization = 0.0;

        for (Cloudlet cloudlet : cloudletList) {
            totalExecutionTime += cloudlet.getActualCPUTime();
            Vm vm = vmList.get(cloudlet.getVmId());

            // Simple cost calculation for processing, memory, and bandwidth usage.
            totalCost += vm.getMips() * 0.03; // Cost per MIPS unit
            totalCost += vm.getRam() * 0.01;  // Cost per MB of RAM
            totalCost += vm.getBw() * 0.005;  // Cost per bandwidth unit

            totalCpuUtilization += cloudlet.getUtilizationOfCpu(CloudSim.clock());
        }

        Log.printLine("\n===== Performance Metrics =====");
        Log.printLine("Total Execution Time: " + totalExecutionTime + " sec");
        Log.printLine("Total Cost: $" + totalCost);
        Log.printLine("Average CPU Utilization: " + totalCpuUtilization / cloudletList.size() * 100 + "%");
    }
}
