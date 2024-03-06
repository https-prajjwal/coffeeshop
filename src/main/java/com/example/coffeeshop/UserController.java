package com.example.coffeeshop;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.ThreadContext.isEmpty;

@Controller
@RequestMapping("/user")
public class UserController {


    private final UserRepository userRepository;
    @Autowired
    private UserService userService;

    private final ItemRepository itemRepository;
    @Autowired
    private ItemService itemService;

    private final OrdersRepository ordersRepository;
    @Autowired
    private OrdersService ordersService;

    private final TransactionRepository transactionRepository;
    @Autowired
    private TransactionService transactionService;



    public UserController(UserRepository userRepository, ItemRepository itemRepository, OrdersRepository ordersRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.ordersRepository = ordersRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/")
    public String getLogin(){
        return "login";
    }

    @GetMapping("/signup")
    public String signup(){
        return "signup";
    }


    @GetMapping("/index")
    public String index(Model model){
        List< Item > items = itemService.getAllItems();
        model.addAttribute("items", items);
        return "index";
    }

    @GetMapping("/index2")
    public String index2(HttpSession session, Model model){
        List<Item> cartItems = (List<Item>)
                session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", calculateTotal(cartItems));
        List< Item > items = itemService.getAllItems();
        model.addAttribute("items", items);
        int totalQuantity = calculateTotalQuantity(cartItems);

        // Add total quantity to the model
        model.addAttribute("totalQuantity", totalQuantity);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
            return "index2";
        } else {
            return "redirect:/user/";
        }
    }


    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String
            password, HttpSession session, Model model) {
        User user = userRepository.findByEmail(email);

        if (email.isEmpty()) {
            model.addAttribute("error", "No email provided");
            return "login";
        }


        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("user", user);
            return "redirect:/user/index2";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        session.removeAttribute("cartItems");
        return "redirect:/user/";
    }


    @PostMapping("/save")
    public String saveWord(@ModelAttribute User user, RedirectAttributes redirectAttributes, @RequestParam String name, @RequestParam String password, @RequestParam String c_password, @RequestParam String email) {

        if (name.isEmpty()) {
            redirectAttributes.addFlashAttribute("successMessage", "No name provided!");
            return "redirect:/user/signup";
        } else if (email.isEmpty()) {
            redirectAttributes.addFlashAttribute("successMessage", "No email provided!");
            return "redirect:/user/signup";
        } else if (password.isEmpty()) {
            redirectAttributes.addFlashAttribute("successMessage", "No password provided!");
            return "redirect:/user/signup";
        } else if (c_password.isEmpty()) {
            redirectAttributes.addFlashAttribute("successMessage", "No confirm password provided!");
            return "redirect:/user/signup";
        }
        else if (!password.equals(c_password)) {
            redirectAttributes.addFlashAttribute("successMessage", "Passwords do not match");
            return "redirect:/user/signup";
        }
        else{
            userService.saveUser(user);

            redirectAttributes.addFlashAttribute("successMessage", "User successfully saved!");
            return "redirect:/user/signup";
        }
    }


    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        List<Item> cartItems = (List<Item>)
                session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", calculateTotal(cartItems));

        int totalQuantity = calculateTotalQuantity(cartItems);

        // Add total quantity to the model
        model.addAttribute("totalQuantity", totalQuantity);


        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "cart";
    }


    @PostMapping("/add")
    public String addToCart(@RequestParam String name, @RequestParam int price, @RequestParam String image, HttpSession session, Model model) {
        List<Item> cartItems = (List<Item>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
            session.setAttribute("cartItems", cartItems);
        }
        boolean itemExists = false;
        for (Item cartItem : cartItems) {
            if (cartItem.getName().equals(name)) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            cartItems.add(new Item(name, price , image));
        }

        int totalQuantity = calculateTotalQuantity(cartItems);

        // Add total quantity to the model
        model.addAttribute("totalQuantity", totalQuantity);

        return "redirect:/user/index2";
    }

    private int calculateTotalQuantity(List<Item> cartItems) {
        int totalQuantity = 0;
        for (Item item : cartItems) {
            totalQuantity += item.getQuantity();
        }
        return totalQuantity;
    }


    @PostMapping("/remove")
    public String removeFromCart(@RequestParam int index, HttpSession
            session) {
        List<Item> cartItems = (List<Item>)
                session.getAttribute("cartItems");
        if (cartItems != null && index >= 0 && index < cartItems.size()) {
            cartItems.remove(index);
        }
        return "redirect:/user/cart";
    }



    @PostMapping("/transaction")
    public String checkout(@ModelAttribute Orders orders, @RequestParam int user_id, @RequestParam LocalDate date, @RequestParam int total, @RequestParam String status, HttpSession session, Model model) {
        List<Item> cartItems = (List<Item>)
                session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", calculateTotal(cartItems));



        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        if (user.getBalance() >= total){
            user.setBalance(user.getBalance()-total);
            userService.saveUser(user);
            ordersService.saveOrder(orders);

            session.removeAttribute("cartItems");


            cartItems = new ArrayList<>();
            session.setAttribute("cartItems", cartItems);
            int totalQuantity = calculateTotalQuantity(cartItems);

            model.addAttribute("totalQuantity", totalQuantity);

            return "confirm";
        }else{

            int totalQuantity = calculateTotalQuantity(cartItems);

            // Add total quantity to the model
            model.addAttribute("totalQuantity", totalQuantity);
            return "insufficient";
        }
    }


    @PostMapping("/deposit")
    public String deposit(@ModelAttribute Transaction transaction,
                          @RequestParam(required = false) Integer debit,
                          @RequestParam int user_id,
                          @RequestParam(required = false) LocalDate date,
                          @RequestParam(required = false) BigInteger card_num,
                          @RequestParam(required = false) String name,
                          @RequestParam(required = false) String exp,
                          @RequestParam(required = false) String cvv,
                          HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        if (debit == null || debit <= 0) {
            redirectAttributes.addFlashAttribute("successMessage", "Invalid or no amount provided!");
            return "redirect:/user/insufficient";
        } else if (card_num == null || card_num.toString().length() != 16) {
            redirectAttributes.addFlashAttribute("successMessage", "Invalid or no card number provided!");
            return "redirect:/user/insufficient";
        } else if (cvv == null || cvv.isEmpty()) {
            redirectAttributes.addFlashAttribute("successMessage", "Invalid or no CVV provided!");
            return "redirect:/user/insufficient";
        } else if (exp == null || exp.isEmpty()) {
            redirectAttributes.addFlashAttribute("successMessage", "Invalid or no expiry date provided!");
            return "redirect:/user/insufficient";
        } else if (name == null || name.isEmpty()) {
            redirectAttributes.addFlashAttribute("successMessage", "Invalid or no name provided");
            return "redirect:/user/insufficient";
        } else if (date == null) {
            redirectAttributes.addFlashAttribute("successMessage", "Invalid or no date provided");
            return "redirect:/user/insufficient";
        } else {

            User user = (User) session.getAttribute("user");
            model.addAttribute("user", user);
            user.setBalance(user.getBalance() + debit);
            userService.saveUser(user);
            transactionService.saveTransaction(transaction);
            return "redirect:/user/cart";
        }
    }






    @GetMapping("/insufficient")
    public String page(HttpSession session, Model model){
        List<Item> cartItems = (List<Item>)
                session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", calculateTotal(cartItems));

        int totalQuantity = calculateTotalQuantity(cartItems);

        // Add total quantity to the model
        model.addAttribute("totalQuantity", totalQuantity);
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "insufficient";
    }

    private int calculateTotal(List<Item> cartItems) {
        int total = 0;
        for (Item item : cartItems) {
            total += item.getPrice()* item.getQuantity();
        }
        return total;
    }

    @PostMapping("/updateQuantity")
    public String updateQuantity(@RequestParam int index, @RequestParam String action, HttpSession session) {
        List<Item> cartItems = (List<Item>) session.getAttribute("cartItems");

        if (cartItems != null && index >= 0 && index < cartItems.size()) {
            Item cartItem = cartItems.get(index);

            if ("increase".equals(action)) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
            } else if ("decrease".equals(action) && cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
            }
            session.setAttribute("cartItems", cartItems);
        }

        // Redirect back to the cart page
        return "redirect:/user/cart";
    }


    @GetMapping("/confirm")
    public String showConfirm(){
        return "confirm";
    }
}
