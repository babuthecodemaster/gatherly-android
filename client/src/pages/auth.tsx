import { useState } from "react";
import { useLocation } from "wouter";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useAuth } from "@/hooks/use-auth";
import { Rocket, Star, Sparkles } from "lucide-react";

const loginSchema = z.object({
  email: z.string().email("Please enter a valid email"),
  password: z.string().min(6, "Password must be at least 6 characters"),
});

const registerSchema = z.object({
  username: z.string().min(3, "Username must be at least 3 characters"),
  email: z.string().email("Please enter a valid email"),
  password: z.string().min(6, "Password must be at least 6 characters"),
});

type LoginFormData = z.infer<typeof loginSchema>;
type RegisterFormData = z.infer<typeof registerSchema>;

export default function AuthPage() {
  const [, setLocation] = useLocation();
  const { login, register: registerUser } = useAuth();
  const [activeTab, setActiveTab] = useState("login");

  const loginForm = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const registerForm = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: "",
      email: "",
      password: "",
    },
  });

  const onLogin = async (data: LoginFormData) => {
    try {
      await login(data);
      setLocation("/");
    } catch (error) {
      // Error handled by auth hook
    }
  };

  const onRegister = async (data: RegisterFormData) => {
    try {
      await registerUser(data);
      setLocation("/");
    } catch (error) {
      // Error handled by auth hook
    }
  };

  return (
    <div className="min-h-screen bg-cosmic-black flex items-center justify-center p-4 nebula-bg">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center space-x-2 mb-4">
            <div className="w-12 h-12 bg-gradient-to-br from-cosmic-blue to-cosmic-purple rounded-2xl flex items-center justify-center cosmic-glow animate-pulse-glow">
              <Rocket className="w-6 h-6 text-white" />
            </div>
            <h1 className="text-3xl font-bold cosmic-text-gradient">Gatherly</h1>
          </div>
          <p className="text-cosmic-gray">Welcome to the cosmic chat experience</p>
        </div>

        <Card className="bg-cosmic-navy border-gray-700 cosmic-glow">
          <CardHeader className="text-center">
            <CardTitle className="text-white">Join the Cosmic Community</CardTitle>
            <CardDescription className="text-cosmic-gray">
              Connect with space enthusiasts and cosmic explorers
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Tabs value={activeTab} onValueChange={setActiveTab}>
              <TabsList className="grid w-full grid-cols-2 bg-gray-800">
                <TabsTrigger value="login" className="data-[state=active]:bg-cosmic-blue">
                  Login
                </TabsTrigger>
                <TabsTrigger value="register" className="data-[state=active]:bg-cosmic-purple">
                  Register
                </TabsTrigger>
              </TabsList>

              <TabsContent value="login" className="space-y-4">
                <form onSubmit={loginForm.handleSubmit(onLogin)} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="login-email" className="text-white">
                      Email
                    </Label>
                    <Input
                      id="login-email"
                      type="email"
                      placeholder="cosmic@explorer.com"
                      className="bg-gray-800 border-gray-600 text-white placeholder-cosmic-gray focus:border-cosmic-blue"
                      data-testid="input-login-email"
                      {...loginForm.register("email")}
                    />
                    {loginForm.formState.errors.email && (
                      <p className="text-red-400 text-sm">
                        {loginForm.formState.errors.email.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="login-password" className="text-white">
                      Password
                    </Label>
                    <Input
                      id="login-password"
                      type="password"
                      placeholder="••••••••"
                      className="bg-gray-800 border-gray-600 text-white placeholder-cosmic-gray focus:border-cosmic-blue"
                      data-testid="input-login-password"
                      {...loginForm.register("password")}
                    />
                    {loginForm.formState.errors.password && (
                      <p className="text-red-400 text-sm">
                        {loginForm.formState.errors.password.message}
                      </p>
                    )}
                  </div>

                  <Button
                    type="submit"
                    className="w-full bg-cosmic-blue hover:bg-blue-600 text-white cosmic-glow"
                    disabled={loginForm.formState.isSubmitting}
                    data-testid="button-login"
                  >
                    {loginForm.formState.isSubmitting ? (
                      <>
                        <Star className="w-4 h-4 mr-2 animate-spin" />
                        Connecting to the cosmos...
                      </>
                    ) : (
                      <>
                        <Rocket className="w-4 h-4 mr-2" />
                        Launch into Chat
                      </>
                    )}
                  </Button>
                </form>
              </TabsContent>

              <TabsContent value="register" className="space-y-4">
                <form onSubmit={registerForm.handleSubmit(onRegister)} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="register-username" className="text-white">
                      Username
                    </Label>
                    <Input
                      id="register-username"
                      type="text"
                      placeholder="CosmicExplorer"
                      className="bg-gray-800 border-gray-600 text-white placeholder-cosmic-gray focus:border-cosmic-purple"
                      data-testid="input-register-username"
                      {...registerForm.register("username")}
                    />
                    {registerForm.formState.errors.username && (
                      <p className="text-red-400 text-sm">
                        {registerForm.formState.errors.username.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="register-email" className="text-white">
                      Email
                    </Label>
                    <Input
                      id="register-email"
                      type="email"
                      placeholder="cosmic@explorer.com"
                      className="bg-gray-800 border-gray-600 text-white placeholder-cosmic-gray focus:border-cosmic-purple"
                      data-testid="input-register-email"
                      {...registerForm.register("email")}
                    />
                    {registerForm.formState.errors.email && (
                      <p className="text-red-400 text-sm">
                        {registerForm.formState.errors.email.message}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="register-password" className="text-white">
                      Password
                    </Label>
                    <Input
                      id="register-password"
                      type="password"
                      placeholder="••••••••"
                      className="bg-gray-800 border-gray-600 text-white placeholder-cosmic-gray focus:border-cosmic-purple"
                      data-testid="input-register-password"
                      {...registerForm.register("password")}
                    />
                    {registerForm.formState.errors.password && (
                      <p className="text-red-400 text-sm">
                        {registerForm.formState.errors.password.message}
                      </p>
                    )}
                  </div>

                  <Button
                    type="submit"
                    className="w-full bg-cosmic-purple hover:bg-purple-600 text-white cosmic-glow-purple"
                    disabled={registerForm.formState.isSubmitting}
                    data-testid="button-register"
                  >
                    {registerForm.formState.isSubmitting ? (
                      <>
                        <Sparkles className="w-4 h-4 mr-2 animate-spin" />
                        Creating cosmic identity...
                      </>
                    ) : (
                      <>
                        <Star className="w-4 h-4 mr-2" />
                        Join the Galaxy
                      </>
                    )}
                  </Button>
                </form>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>

        {/* Decorative elements */}
        <div className="flex justify-center mt-8 space-x-4">
          <div className="w-2 h-2 bg-cosmic-blue rounded-full animate-pulse"></div>
          <div className="w-2 h-2 bg-cosmic-purple rounded-full animate-pulse delay-75"></div>
          <div className="w-2 h-2 bg-cosmic-blue rounded-full animate-pulse delay-150"></div>
        </div>
      </div>
    </div>
  );
}
