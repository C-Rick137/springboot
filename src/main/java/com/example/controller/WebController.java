package com.example.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Employee;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.ArticleService;
import com.example.service.EmployeeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class WebController {

    @Resource
    private EmployeeService employeeService;

    @Resource
    private AdminService adminService;

    @Resource
    private ArticleService articleService;

    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account result = null;
        if ("ADMIN".equals(account.getRole())) {
            result = adminService.login(account);
        } else if ("EMP".equals(account.getRole())) {
            result = employeeService.login(account);
        } else {
            throw new CustomException("500", "非法输入");
        }
        return Result.success(result);
    }

    @PostMapping("/register")
    public Result register(@RequestBody Employee employee) {
        employeeService.register(employee);
        return Result.success();
    }

    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("ADMIN".equals(account.getRole())) {
            adminService.updatePassword(account);
        } else if ("EMP".equals(account.getRole())) {
            employeeService.updatePassword(account);
        } else {
            throw new CustomException("500", "非法输入");
        }
        return Result.success();
    }

    @GetMapping("/barData")
    public Result getBarData() {
        Map<String, Object> map = new HashMap<>();
        List<Employee> employeeList = employeeService.selectAll(null);
        Set<String> departmentNameSet = employeeList.stream().map(Employee::getDepartmentName).collect(Collectors.toSet());
        map.put("department", departmentNameSet);
        List<Long> countList = new ArrayList<>();
        for (String departmentName : departmentNameSet) {
            long count = employeeList.stream().filter(employee -> employee.getDepartmentName().equals(departmentName)).count();
            countList.add(count);
        }
        map.put("count", countList);
        return Result.success(map);
    }

    @GetMapping("/lineData")
    public Result getLineData() {
        Map<String, Object> map = new HashMap<>();
        Date date = new Date();
        DateTime start = DateUtil.offsetDay(date, -7);
        List<DateTime> dateTimeList = DateUtil.rangeToList(start, date, DateField.DAY_OF_MONTH);
        List<String> dateStrList = dateTimeList.stream().map(dateTime -> DateUtil.format(dateTime, "MM月dd日")).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        map.put("date", dateStrList);
        List<Integer> countList = new ArrayList<>();
        for (DateTime day : dateTimeList) {
            String dayFormat = DateUtil.formatDate(day);
            Integer count = articleService.selectCountByDate(dayFormat);
            countList.add(count);
        }
        map.put("count", countList);
        return Result.success(map);
    }

    @GetMapping("/pieData")
    public Result getPieData() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Employee> employeeList = employeeService.selectAll(null);
        Set<String> departmentNameSet = employeeList.stream().map(Employee::getDepartmentName).collect(Collectors.toSet());
        for (String departmentName : departmentNameSet) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", departmentName);
            long count = employeeList.stream().filter(employee -> employee.getDepartmentName().equals(departmentName)).count();
            map.put("value", count);
            list.add(map);
        }
        return Result.success(list);
    }
}
